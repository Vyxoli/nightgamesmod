package nightgames.skills;

import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.Player;
import nightgames.characters.Trait;
import nightgames.characters.body.BodyPart;
import nightgames.combat.Combat;
import nightgames.combat.Result;
import nightgames.global.Global;
import nightgames.stance.Stance;
import nightgames.status.BodyFetish;
import nightgames.status.addiction.AddictionType;

public class Thrust extends Skill {
    public Thrust(String name, Character self) {
        super(name, self);
    }

    public Thrust(Character self) {
        super("Thrust", self);
    }

    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return !user.has(Trait.temptress) || user.get(Attribute.Technique) < 11;
    }

    @Override
    public boolean usable(Combat c, Character target) {
        return getSelfOrgan(c) != null && getTargetOrgan(c, target) != null && getSelf().canAct()
                        && c.getStance().canthrust(getSelf()) && c.getStance().havingSexOtherNoStrapped(getSelf());
    }

    public BodyPart getSelfOrgan(Combat c) {
        if (c.getStance().inserted(getSelf())) {
            return getSelf().body.getRandomInsertable();
        } else if (c.getStance().anallyPenetratedBy(getSelf(), c.getOther(getSelf()))) {
            return getSelf().body.getRandom("ass");
        } else {
            return getSelf().body.getRandomPussy();
        }
    }

    public BodyPart getTargetOrgan(Combat c, Character target) {
        if (c.getStance().inserted(target)) {
            return target.body.getRandomInsertable();
        } else if (c.getStance().anallyPenetratedBy(c.getOther(getSelf()), getSelf())) {
            return target.body.getRandom("ass");
        } else {
            return target.body.getRandomPussy();
        }
    }

    public int[] getDamage(Combat c, Character target) {
        int results[] = new int[2];

        int m = 5 + Global.random(14);
        if (c.getStance().anallyPenetrated(target) && getSelf().has(Trait.assmaster)) {
            m *= 1.5;
        }
        
        float mt = Math.max(1, m / 3.f);

        if (getSelf().has(Trait.experienced)) {
            mt = Math.max(1, mt * .66f);
        }
        mt = target.modRecoilPleasure(c, mt);

        if (getSelf().human() || target.human()) {
            Player p = Global.getPlayer();
            Character npc = c.getOther(p);
            if (p.checkAddiction(AddictionType.BREEDER, npc)) {
                float bonus = .3f * p.getAddiction(AddictionType.BREEDER).getCombatSeverity().ordinal();
                if (p == getSelf()) {
                    mt += mt * bonus;
                } else {
                    m += m * bonus;                    
                }
            }
        }
        
        results[0] = m;
        results[1] = (int) mt;

        return results;
    }

    @Override
    public boolean resolve(Combat c, Character target) {
        BodyPart selfO = getSelfOrgan(c);
        BodyPart targetO = getTargetOrgan(c, target);
        Result result;
        if (c.getStance().inserted(target)) {
            result = Result.reverse;
        } else if (c.getStance().en == Stance.anal) {
            result = Result.anal;
        } else {
            result = Result.normal;
        }

        if (getSelf().human()) {
            c.write(getSelf(), deal(c, 0, result, target));
        } else if (target.human()) {
            c.write(getSelf(), receive(c, 0, result, target));
        }

        int[] m = getDamage(c, target);
        assert m.length >= 2;

        if (m[0] != 0) {
            target.body.pleasure(getSelf(), selfO, targetO, m[0], c);
        }
        if (m[1] != 0) {
            getSelf().body.pleasure(target, targetO, selfO, m[1], c);
        }
        if (selfO.isType("ass") && Global.random(100) < 2 + getSelf().get(Attribute.Fetish)) {
            target.add(c, new BodyFetish(target, getSelf(), "ass", .25));
        }
        return true;
    }

    @Override
    public int getMojoBuilt(Combat c) {
        return 0;
    }

    @Override
    public Skill copy(Character user) {
        return new Thrust(user);
    }

    @Override
    public Tactics type(Combat c) {
        return Tactics.fucking;
    }

    @Override
    public String deal(Combat c, int damage, Result modifier, Character target) {
        if (modifier == Result.anal) {
            return "You thrust steadily into " + target.name() + "'s ass, eliciting soft groans of pleasure.";
        } else if (modifier == Result.reverse) {
            return Global.format(
                            "You rock your hips against {other:direct-object}, riding her smoothly. "
                                            + "Despite the slow place, {other:subject} soon starts gasping and mewing with pleasure.",
                            getSelf(), target);
        } else {
            return "You thrust into " + target.name()
                            + " in a slow, steady rhythm. She lets out soft breathy moans in time with your lovemaking. You can't deny you're feeling "
                            + "it too, but by controlling the pace, you can hopefully last longer than she can.";
        }
    }

    @Override
    public String receive(Combat c, int damage, Result modifier, Character target) {
        if (modifier == Result.anal) {
            if (getSelf().has(Trait.strapped)) {
                String res = getSelf().name()
                                + " thrusts her hips, pumping her artificial cock in and out of your ass and pushing on your prostate.";
                if (getSelf().has(Trait.assmaster)) {
                    return res + getSelf().name()
                                    + "'s penchant for fucking people in the ass makes her thrusting that much more powerful, and that much more intense for the both of you.";
                }
                return res;
            } else {
                return getSelf().name() + "'s cock slowly pumps the inside of your rectum.";
            }
        } else if (modifier == Result.reverse) {
            return getSelf().name()
                            + " rocks her hips against you, riding you smoothly and deliberately. Despite the slow pace, the sensation of her hot "
                            + getSelfOrgan(c).fullDescribe(getSelf()) + " surrounding "
                            + "your dick is gradually driving you to your limit.";
        } else {
            return Global.format(
                            "{self:subject} thrusts into {other:name-possessive} {other:body-part:pussy} in a slow steady rhythm, leaving you gasping.",
                            getSelf(), target);
        }
    }

    @Override
    public String describe(Combat c) {
        return "Slow fuck, minimizes own pleasure";
    }

    @Override
    public String getName(Combat c) {
        if (c.getStance().inserted(getSelf())) {
            return "Thrust";
        } else {
            return "Ride";
        }
    }

    @Override
    public boolean makesContact() {
        return true;
    }
}
