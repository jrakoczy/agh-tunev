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

package edu.agh.tunev.model;

import java.awt.geom.Point2D;

/**
 * Reprezentuje właściwości jakiejś osoby niezależne od używanego modelu.
 * 
 * Instancje tworzone przez użytkownika oprogramowania.
 * 
 * Użytkownik może użyć dokładnie tych samych profili na kilku modelach i
 * prawdopodobnie zrobi to, żeby porównać wyniki. Dlatego, dla bezpieczeństwa,
 * wszystkie pola klasy są niezmienne.
 * 
 */
public final class PersonProfile {

	/** Szerokość osoby (OX) [m] */
	public final static double WIDTH = 0.45;

	/** Grubość osoby (OY) [m] */
	public final static double GIRTH = 0.27;
	
	/** Pozycja początkowa */
	public final Point2D.Double initialPosition;

	/** Orientacja początkowa */
	public final double initialOrientation = 0.0;

	/** Początkowy sposób ruchu */
	public final PersonState.Movement initialMovement = PersonState.Movement.STANDING;

	/** Wysokość osoby (OZ) [m] */
	public final double height = 1.7;

	public PersonProfile(Point2D.Double initialPosition) {
		this.initialPosition = initialPosition;
	}

}
