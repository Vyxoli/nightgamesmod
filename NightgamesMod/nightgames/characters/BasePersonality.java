package nightgames.characters;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import nightgames.actions.Action;
import nightgames.actions.Movement;
import nightgames.characters.body.BodyPart;
import nightgames.characters.body.CockMod;
import nightgames.characters.body.CockPart;
import nightgames.characters.custom.AiModifiers;
import nightgames.characters.custom.CommentSituation;
import nightgames.characters.custom.RecruitmentData;
import nightgames.combat.Combat;
import nightgames.global.Flag;
import nightgames.global.Global;
import nightgames.items.Item;
import nightgames.skills.Skill;
import nightgames.start.NpcConfiguration;

public abstract class BasePersonality implements Personality {
    /**
     *
     */
    private static final long serialVersionUID = 2279220186754458082L;
    private String type;
    public NPC character;
    protected Growth growth;
    protected List<PreferredAttribute> preferredAttributes;
    protected CockMod preferredCockMod;
    protected AiModifiers mods;

    public interface PreferredAttribute {
        Optional<Attribute> getPreferred(Character c);
    }

    protected BasePersonality() {
    }

    public BasePersonality(String name, int level, Optional<NpcConfiguration> charConfig,
                    Optional<NpcConfiguration> commonConfig) {
        // Make the built-in character
        type = getClass().getSimpleName();
        character = new NPC(name, level, this);
        applyBasicStats();
        growth = new Growth();
        preferredCockMod = CockMod.error;
        preferredAttributes = new ArrayList<PreferredAttribute>();
        setGrowth();
        character.body.makeGenitalOrgans(character.initialGender);

        // Apply config changes
        Optional<NpcConfiguration> mergedConfig = NpcConfiguration.mergeOptionalNpcConfigs(charConfig, commonConfig);
        mergedConfig.ifPresent(cfg -> cfg.apply(character));

        character.body.finishBody(character.initialGender);
    }

    /**
     * Apply built-in character stats. Can be later overridden by StartConfiguration.
     */
    // TODO: Make this data-driven, like with custom NPCs.
    protected abstract void applyBasicStats();
    
    public void setCharacter(NPC c) {
        this.character = c;
    }

    abstract public void setGrowth();

    @Override
    public void rest(int time) {
        if (preferredCockMod != CockMod.error && character.rank > 0) {
            Optional<BodyPart> optDick = character.body.get("cock")
                                                       .stream()
                                                       .filter(part -> part.getMod(character) != preferredCockMod)
                                                       .findAny();
            if (optDick.isPresent()) {
                CockPart part = (CockPart) optDick.get();
                character.body.remove(part);
                character.body.add(part.applyMod(preferredCockMod));
            }
        }
    }

    public void buyUpTo(Item item, int number) {
        while (character.money > item.getPrice() && character.count(item) < 3) {
            character.money -= item.getPrice();
            character.gain(item);
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Skill act(HashSet<Skill> available, Combat c) {
        HashSet<Skill> tactic = new HashSet<Skill>();
        Skill chosen;
        ArrayList<WeightedSkill> priority = Decider.parseSkills(available, c, character);
        if (!Global.checkFlag(Flag.dumbmode)) {
            chosen = character.prioritizeNew(priority, c);
        } else {
            chosen = character.prioritize(priority);
        }
        if (chosen == null) {
            tactic = available;
            Skill[] actions = tactic.toArray(new Skill[tactic.size()]);
            return actions[Global.random(actions.length)];
        } else {
            return chosen;
        }
    }

    @Override
    public Action move(HashSet<Action> available, HashSet<Movement> radar) {
        Action proposed = Decider.parseMoves(available, radar, character);
        return proposed;
    }

    @Override
    public void pickFeat() {
        ArrayList<Trait> available = new ArrayList<Trait>();
        for (Trait feat : Global.getFeats(character)) {
            if (!character.has(feat)) {
                available.add(feat);
            }
        }
        if (available.size() == 0) {
            return;
        }
        character.add((Trait) available.toArray()[Global.random(available.size())]);
    }

    @Override
    public String image(Combat c) {
        String fname = getType()
                                .toLowerCase()
                        + "_" + character.mood.name() + ".jpg";
        return fname;
    }

    public String defaultImage(Combat c) {
        return character.name()
                        .toLowerCase() + "_confident.jpg";
    }

    public Growth getGrowth() {
        return growth;
    }

    @Override
    public void ding() {
        growth.levelUp(character);
        distributePoints();
    }

    @Override
    public String describeAll(Combat c) {
        StringBuilder b = new StringBuilder();
        b.append(describe(c));
        b.append("<br><br>");
        character.body.describe(b, character, " ");
        b.append("<br>");
        for (Trait t : character.getTraits()) {
            t.describe(character, b);
            b.append(' ');
        }
        b.append("<br>");
        return b.toString();
    }

    @Override
    public NPC getCharacter() {
        return character;
    }

    public void distributePoints() {
        if (character.availableAttributePoints <= 0) {
            return;
        }
        ArrayList<Attribute> avail = new ArrayList<Attribute>();
        Deque<PreferredAttribute> preferred = new ArrayDeque<PreferredAttribute>(preferredAttributes);
        for (Attribute a : character.att.keySet()) {
            if (Attribute.isTrainable(a, character) && (character.getPure(a) > 0 || Attribute.isBasic(a))) {
                avail.add(a);
            }
        }
        if (avail.size() == 0) {
            avail.add(Attribute.Cunning);
            avail.add(Attribute.Power);
            avail.add(Attribute.Seduction);
        }
        int noPrefAdded = 2;
        for (; character.availableAttributePoints > 0; character.availableAttributePoints--) {
            Attribute selected = null;
            // remove all the attributes that isn't in avail
            preferred = new ArrayDeque<>(preferred.stream()
                                                  .filter(p -> {
                                                      Optional<Attribute> att = p.getPreferred(character);
                                                      return att.isPresent() && avail.contains(att.get());
                                                  })
                                                  .collect(Collectors.toList()));
            if (preferred.size() > 0) {
                if (noPrefAdded > 1) {
                    noPrefAdded = 0;
                    Optional<Attribute> pref = preferred.removeFirst()
                                                        .getPreferred(character);
                    if (pref.isPresent()) {
                        selected = pref.get();
                    }
                } else {
                    noPrefAdded += 1;
                }
            }

            if (selected == null) {
                selected = avail.get(Global.random(avail.size()));
            }
            character.mod(selected, 1);
            selected = null;
        }
    }

    @Override
    public RecruitmentData getRecruitmentData() {
        return null;
    }

    @Override
    public AiModifiers getAiModifiers() {
        if (mods == null)
            resetAiModifiers();
        return mods;
    }
    
    @Override
    public void setAiModifiers(AiModifiers mods) {
        this.mods = mods;
    }
    
    @Override
    public void resetAiModifiers() {
        mods = AiModifiers.getDefaultModifiers(getType());
    }
    
    @Override
    public String resist3p(Combat c, Character target, Character assist) {
        return null;
    }

    @Override
    public Map<CommentSituation, String> getComments(Combat c) {
        Map<CommentSituation, String> all = CommentSituation.getDefaultComments(getType());
        Map<CommentSituation, String> applicable = new HashMap<>();
        all.entrySet()
           .stream()
           .filter(e -> e.getKey()
                         .isApplicable(c, character, c.getOther(character)))
           .forEach(e -> applicable.put(e.getKey(), e.getValue()));
        return applicable;
    }
}
