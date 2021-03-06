/*
 * Copyright 2013 Kuba Rakoczy, Michał Rus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package edu.agh.tunev.model.jrakoczy;

import java.util.Vector;

import edu.agh.tunev.model.AbstractModel;
import edu.agh.tunev.model.PersonProfile;
import edu.agh.tunev.model.PersonState;
import edu.agh.tunev.statistics.HbCOStatistics;
import edu.agh.tunev.statistics.LifeStatistics;
import edu.agh.tunev.statistics.Statistics.AddCallback;
import edu.agh.tunev.statistics.VelocityStatistics;
import edu.agh.tunev.world.World;
import edu.agh.tunev.world.World.ProgressCallback;

public final class Model extends AbstractModel {

	public final static String MODEL_NAME = "Continuous Space Discreet Time (Experimental)";

	public Model(World world) {
		super(world);
	}

	private static final double DT = 0.05;

	@Override
	public void simulate(double duration, Vector<PersonProfile> profiles,
			ProgressCallback progressCallback, AddCallback addCallback) {
		// number of iterations
		int num = (int) Math.round(Math.ceil(world.getDuration() / DT));
		progressCallback.update(0, num, "Initializing...");

		// init charts
		LifeStatistics lifeStatistics = new LifeStatistics();
		addCallback.add(lifeStatistics);
		VelocityStatistics velocityStatistics = new VelocityStatistics();
		addCallback.add(velocityStatistics);
		HbCOStatistics hcboStatistics = new HbCOStatistics();
		addCallback.add(hcboStatistics);

		// init board
		Board board = new Board(world);

		// init people
		for (PersonProfile profile : profiles)
			board.addAgent(new Agent(board, profile));

		// simulate
		double t = 0;
		for (int iteration = 1; iteration <= num; iteration++) {
			// update
			t += DT;
			board.update(t, DT);

			// chart inputs
			int currentNumDead = 0;
			int currentNumAlive = 0;
			int currentNumRescued = 0;
			double averageVelocity = 0;
			double averageHCBO = 0;

			// save states
			for (Agent p : board.getAgents()) {
				averageVelocity += p.getVelocity();
				averageHCBO += p.getHBCO();
				
				PersonState.Movement stance = p.getStance(); 
				if (p.isExited()) {
					currentNumRescued++;
					stance = PersonState.Movement.HIDDEN;
				}
				else if (!p.isAlive()) {
					currentNumDead++;
					stance = PersonState.Movement.DEAD;
				}
				else
					currentNumAlive++;
				interpolator.saveState(p.profile, t, new PersonState(
						p.position, p.phi, stance));
			}
			averageVelocity /= board.getAgents().size();
			averageHCBO /= board.getAgents().size();

			// update charts
			lifeStatistics.add(t, currentNumAlive, currentNumRescued,
					currentNumDead);
			velocityStatistics.add(t, averageVelocity);
			hcboStatistics.add(t, averageHCBO);

			// UI simulation progress bar
			progressCallback.update(iteration, num,
					(iteration < num ? "Simulating..." : "Done."));
		}
	}

}
