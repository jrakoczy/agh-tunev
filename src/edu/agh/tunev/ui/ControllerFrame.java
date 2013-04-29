package edu.agh.tunev.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.Vector;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;

import edu.agh.tunev.interpolation.Interpolator;
import edu.agh.tunev.model.AbstractModel;
import edu.agh.tunev.model.AbstractPerson;
import edu.agh.tunev.ui.opengl.Refresher;
import edu.agh.tunev.ui.opengl.Scene;
import edu.agh.tunev.ui.plot.AbstractPlot;
import edu.agh.tunev.world.World;

class ControllerFrame extends JInternalFrame {

	static {
		GLProfile.initSingleton();
	}

	private static final long serialVersionUID = 1L;

	private AbstractModel<? extends AbstractPerson> model;
	private Class<?> personClass;
	Vector<AbstractPerson> people;
	World world;
	Interpolator interpolator = new Interpolator();

	private int modelNumber;
	private String modelName;

	ControllerFrame(int modelNumber, String modelName, Class<?> model,
			final World world) {
		this.modelNumber = modelNumber;
		this.modelName = modelName;
		this.world = world;

		setModel(model);

		people = PeopleFactory.random(personClass, 50, world.getXDimension(),
				world.getYDimension());

		init();
		createGLFrame();
	}

	@SuppressWarnings("unchecked")
	void setModel(Class<?> model) {
		try {
			Type personType = ((ParameterizedType) model.getGenericSuperclass())
					.getActualTypeArguments()[0];
			if (!Class.class.isInstance(personType))
				throw new IllegalArgumentException(model.getName()
						+ "'s generic parameter is not a class.");
			Class<?> personClass = (Class<?>) personType;
			if (!AbstractPerson.class.isAssignableFrom(personClass))
				throw new IllegalArgumentException(model.getName()
						+ "'s generic parameter is not an AbstractPerson.");
			this.personClass = personClass;

			this.model = (AbstractModel<? extends AbstractPerson>) model
					.getDeclaredConstructor(World.class, Interpolator.class)
					.newInstance(world, interpolator);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error during instantiation of "
					+ model.getName() + ".");
		}
	}

	private static final Insets INSETS = new Insets(5, 5, 5, 5);
	private static final double DT = 0.01;

	private JButton buttonPlay, buttonStop;
	private JLabel simulationMsg, simulationIter, simulationTime, playbackTime;
	private JProgressBar simulationProgress;
	private JSlider slider;
	private double sliderTime = 0.0, progressTime = 0.0;
	private DecimalFormat decimalFormat = new DecimalFormat("0.00");
	private Refresher refresher;
	private GLWindow glwindow;
	private NewtCanvasAWT glcanvas;

	private void createGLFrame() {
		// in a new thread, because loading of JOGL takes some time, we don't
		// want to put this on AWT thread and block UI
		new Thread(new Runnable() {
			public void run() {
				GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
				caps.setSampleBuffers(true);
				glwindow = GLWindow.create(caps);
				glcanvas = new NewtCanvasAWT(glwindow);

				glwindow.addGLEventListener(new Scene(world, interpolator,
						new Scene.TimeGetter() {
							public double get() {
								return sliderTime;
							}
						}));

				refresher = new Refresher(glwindow);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JInternalFrame frame = new JInternalFrame();

						frame.setTitle(modelNumber + ": " + modelName + " - "
								+ " visualization");
						frame.setSize(400, 300);
						frame.setLocation(modelNumber * 20 + 400,
								modelNumber * 20);
						frame.setFrameIcon(null);
						frame.setResizable(true);

						frame.getContentPane().add(glcanvas,
								BorderLayout.CENTER);
						ControllerFrame.this.getParent().add(frame);
						frame.setVisible(true);

						// simulate after both frames were loaded
						simulate();
					}
				});
			}
		}).start();
	}

	private void init() {
		setTitle(modelNumber + ": " + modelName + " - controller");
		setFrameIcon(null);
		setLocation(modelNumber * 20, modelNumber * 20);

		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(INSETS));
		p.setLayout(new GridBagLayout());
		add(p, BorderLayout.PAGE_START);

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridy = -1;

		// column struts

		c.gridy++;
		for (c.gridx = 0; c.gridx < 5; c.gridx++)
			p.add(Box.createHorizontalStrut(10000), c);
		c.insets = INSETS;

		// simulation labels

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 0;

		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		p.add(new JLabel("Simulation:"), c);

		c.gridx += c.gridwidth;
		c.gridwidth = 2;
		simulationMsg = new JLabel();
		p.add(simulationMsg, c);

		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		simulationIter = new JLabel();
		p.add(simulationIter, c);

		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		simulationTime = new JLabel();
		p.add(simulationTime, c);

		// simulation progress

		c.gridwidth = 5;
		c.gridx = 0;
		c.gridy++;
		simulationProgress = new JProgressBar();
		p.add(simulationProgress, c);

		// separator

		c.gridwidth = 1;
		c.gridy++;
		p.add(new JPanel(), c);

		// playback labels

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 0;

		c.gridx += c.gridwidth;
		c.gridwidth = 4;
		p.add(new JLabel("Playback:"), c);

		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		playbackTime = new JLabel("abc");
		p.add(playbackTime, c);

		// playback slider

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 5;
		slider = new JSlider(0, (int) Math.round(Math.ceil(world.getDuration()
				/ DT)), 0);
		p.add(slider, c);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				onSliderChange();
			}
		});
		onSliderChange();

		// separator

		c.gridwidth = 1;
		c.gridy++;
		p.add(new JPanel(), c);

		// buttons

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 0;

		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		buttonPlay = new JButton("Play");
		p.add(buttonPlay, c);

		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		buttonStop = new JButton("Stop");
		buttonStop.setEnabled(false);
		p.add(buttonStop, c);

		buttonPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buttonPlay.setEnabled(false);
				slider.setEnabled(false);
				buttonStop.setEnabled(true);
				play(1.0);
			}
		});
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					playThread.interrupt();
				} catch (Exception e) {
				}
			}
		});

		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		final JButton buttonPlot = new JButton("Plot...");
		p.add(buttonPlot, c);

		final JPopupMenu plotMenu = new JPopupMenu();

		buttonPlot.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				plotMenu.show(buttonPlot, 0, buttonPlot.getHeight());
			}
		});

		for (final Entry<String, Class<?>> e : MainFrame.plots.entrySet())
			plotMenu.add(new JMenuItem(new AbstractAction(e.getKey()) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent arg0) {
					plot(e.getValue());
				}
			}));

		setVisible(true);
		pack();
		setSize(400, getSize().height);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					ControllerFrame.this.setSelected(true);
				} catch (PropertyVetoException e) {
					ControllerFrame.this.toFront();
					slider.requestFocus();
				}
			}
		});
	}

	private double previousSliderTime = Double.POSITIVE_INFINITY;

	private void onSliderChange() {
		sliderTime = Math.min(DT * slider.getValue(), progressTime);
		playbackTime
				.setText("t = " + decimalFormat.format(sliderTime) + " [s]");

		// refresh visualisation
		if (refresher != null
				&& Math.abs(previousSliderTime - sliderTime) > DT / 2) {
			previousSliderTime = sliderTime;
			refresher.refresh();
		}
	}

	private void onPlayingFinished() {
		buttonPlay.setEnabled(true);
		slider.setEnabled(true);
		buttonStop.setEnabled(false);
	}

	private void simulate() {
		new Thread(new Runnable() {
			public void run() {
				model.simulateWrapper(world.getDuration(), people,
						new World.ProgressCallback() {
							public void update(final int done, final int total,
									final String msg) {
								progressTime = world.getDuration() * done
										/ total;
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										simulationProgress.setMaximum(total);
										simulationProgress.setValue(done);
										simulationMsg.setText(msg);
										simulationIter.setText(done + "/"
												+ total);
										simulationTime.setText("t = "
												+ decimalFormat
														.format(progressTime)
												+ " [s]");

										onSliderChange();
									}
								});
							}
						});
			}
		}).start();
	}

	private Thread playThread = null;

	private void play(double speed) {
		playThread = new Thread(new Runnable() {
			public void run() {
				boolean cont = slider.getValue() < slider.getMaximum();
				try {
					while (cont) {
						Thread.sleep(Math.round(DT * 1000));
						cont = slider.getValue() + 1 < slider.getMaximum();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								slider.setValue(slider.getValue() + 1);
							}
						});
					}
				} catch (InterruptedException e) {
					// probably stop pressed
				}
				onPlayingFinished();
			}
		});
		playThread.start();
	}

	private int plotCounter = 0;

	private void plot(Class<?> type) {
		AbstractPlot plot;
		try {
			plot = (AbstractPlot) type.getDeclaredConstructor(int.class,
					String.class, int.class).newInstance(modelNumber,
					modelName, ++plotCounter);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error during instantiation of "
					+ type.getName() + ".");
		}

		getParent().add(plot);
	}

}
