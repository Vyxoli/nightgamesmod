package nightgames.status;

import org.json.simple.JSONObject;

import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.combat.Combat;
import nightgames.global.Global;
import nightgames.global.JSONUtils;

public class FiredUp extends DurationStatus {

	private int stack;
	private Character other;
	private String part;

	public FiredUp(Character affected, Character other, String part) {
		super("Fired Up", affected, 2);
		this.part = part;
		this.stack = 1;
		flag(Stsflag.firedup);
	}

	public int getStack() {
		return stack;
	}

	public String getPart() {
		return part;
	}

	@Override
	public String initialMessage(Combat c, boolean replaced) {
		return String.format("%s really getting into using %s %s.",
				affected.subjectAction("are", "is"),
				affected.possessivePronoun(), part);
	}

	@Override
	public String describe(Combat c) {
		if (affected.human()) {
			if (stack == 1) {
				return "You are getting a good sense of how to best use your "
						+ part + ".";
			} else if (stack == 2) {
				return String
						.format("The movements of your %s are growing ever more attuned to %s reactions.",
								part, other.nameOrPossessivePronoun());
			} else {
				return String
						.format("You have completely mapped out %s body, and you are finding all of %s most sensitive areas as if by magic.",
								affected.nameOrPossessivePronoun(),
								affected.possessivePronoun());
			}
		} else {
			if (stack == 1) {
				return Global
						.capitalizeFirstLetter(String
								.format("%s has a big grin on %s face at the prospect of further pleasuring you with %s %s.",
										affected.pronoun(),
										affected.possessivePronoun(),
										affected.possessivePronoun(), part));
			} else if (stack == 2) {
				return Global
						.capitalizeFirstLetter(String
								.format("%s looks as if %s is enjoying working %s %s almost as much as you are.",
										affected.pronoun(),
										affected.possessivePronoun(),
										affected.pronoun(), part));
			} else {
				return Global
						.capitalizeFirstLetter(String
								.format("%s is focused almost exclusively on using %s %s to the greatest possible effect, and it's working.",
										affected.pronoun(),
										affected.possessivePronoun(), part));
			}
		}
	}

	@Override
	public int mod(Attribute a) {
		return 0;
	}

	@Override
	public int regen(Combat c) {
		return 0;
	}

	@Override
	public int damage(Combat c, int x) {
		return 0;
	}

	@Override
	public double pleasure(Combat c, double x) {
		return 0;
	}

	@Override
	public float fitnessModifier() {
		return (float) (5 * stack);
	}

	@Override
	public int weakened(int x) {
		return 0;
	}

	@Override
	public int tempted(int x) {
		return 0;
	}

	@Override
	public int evade() {
		return 0;
	}

	@Override
	public int escape() {
		return 0;
	}

	@Override
	public int gainmojo(int x) {
		return stack * 2;
	}

	@Override
	public int spendmojo(int x) {
		return 0;
	}

	@Override
	public int counter() {
		return 0;
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public Status instance(Character newAffected, Character newOther) {
		return new FiredUp(newAffected, newOther, part);
	}

	@Override
	public boolean overrides(Status s) {
		// Replace only if it's the same part.
		return (s instanceof FiredUp) && !((FiredUp) s).part.equals(part);
	}

	@Override
	public void replace(Status s) {
		assert (s instanceof FiredUp);
		stack++;
		setDuration(2);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject saveToJSON() {
		JSONObject obj = new JSONObject();
		obj.put("type", getClass().getSimpleName());
		obj.put("part", part);
		obj.put("stack", stack);
		return obj;
	}

	@Override
	public Status loadFromJSON(JSONObject obj) {
		FiredUp fu = new FiredUp(null, null, JSONUtils.readString(obj, "part"));
		fu.stack = JSONUtils.readInteger(obj, "stack");
		return fu;
	}

}