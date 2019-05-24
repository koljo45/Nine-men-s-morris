package hr.fer.mv.nmm;

import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Describes a generic animation which is made up from steps. Animation starts
 * on the 0. step and is advanced by calling <code>advanceStep()</code>. When
 * the animation reaches the designated number of steps it ends. You can add an
 * {@link ActionListener} which gets executed when the animation ends.
 * 
 * @author Matija Videkoviæ
 *
 */
public abstract class Animation {
	private int stepNumber;
	private int currentStep;
	private List<ActionListener> listeners;
	private boolean eventFired;

	/**
	 * Creates a new animation with the given number of steps.
	 * 
	 * @param stepNumber number of steps to do
	 */
	public Animation(int stepNumber) {
		this.stepNumber = stepNumber;
		currentStep = 0;
		listeners = new ArrayList<>();
		eventFired = false;
	}

	/**
	 * Advances the animation by a single step.
	 * 
	 * @return true if the animation has completed all of its steps, false if not
	 */
	public boolean advanceStep() {
		currentStep = currentStep >= stepNumber ? stepNumber : currentStep + 1;
		if (currentStep >= stepNumber && !eventFired) {
			for (ActionListener al : listeners)
				al.actionPerformed(new ActionEvent(this, 0, ""));
			eventFired = true;
		}
		return currentStep >= stepNumber;
	}

	/**
	 * Checks if the animation has ended, that is if it has reached the designated
	 * number of steps.
	 * 
	 * @return true if the animation has completed all of its steps, false if not
	 */
	public final boolean stepsCompleted() {
		return currentStep >= stepNumber;
	}

	/**
	 * Resets the animation to the 0. step. All the assigned {@link ActionListener}
	 * are removed.
	 */
	public void reset() {
		currentStep = 0;
		eventFired = false;
		listeners.clear();
	}

	/**
	 * Adds an {@link ActionListener} to this animation. When the animation ends
	 * given listener gets executed.
	 * 
	 * @param al listener to be added
	 */
	public void addActionListener(ActionListener al) {
		listeners.add(al);
	}

	/**
	 * Removes an {@link ActionListener} from the animation.
	 * 
	 * @param al listener to be removed
	 * @return true if the listener was present, false if it was not
	 */
	public boolean removeActionListener(ActionListener al) {
		return listeners.remove(al);
	}

}
