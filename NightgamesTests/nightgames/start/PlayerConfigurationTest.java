package nightgames.start;

import nightgames.characters.Attribute;
import nightgames.characters.CharacterSex;
import nightgames.characters.Player;
import nightgames.characters.Trait;
import nightgames.global.JSONUtils;
import nightgames.items.clothing.Clothing;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests involving starting player configuration.
 * Created by Ryplinn on 6/20/2016.
 */
public class PlayerConfigurationTest {
    StartConfiguration startConfig;
    PlayerConfiguration playerConfig;

    @BeforeClass public static void setUpClass() {
        Clothing.buildClothingTable();
    }

    @Before public void setUp() throws Exception {
        Path file = new File("NightgamesTests/nightgames/start/TestStartConfig.json").toPath();
        startConfig = StartConfiguration.parse(JSONUtils.rootFromFile(file));
        playerConfig = startConfig.player;

    }

    @Test public void testPlayerCreation() throws Exception {
        Map<Attribute, Integer> chosenAttributes = new HashMap<>();
        List<Trait> pickedTraits = Arrays.asList(Trait.romantic, Trait.insatiable);
        chosenAttributes.put(Attribute.Power, 5);
        chosenAttributes.put(Attribute.Seduction, 6);
        chosenAttributes.put(Attribute.Cunning, 7);
        Player malePlayer = new Player("dude", CharacterSex.male, Optional.of(playerConfig), pickedTraits,
                        chosenAttributes);
        assertEquals(5, malePlayer.level);
        assertEquals(15000, malePlayer.money);
        assertThat(malePlayer.traits, IsCollectionContaining
                        .hasItems(Trait.pussyhandler, Trait.dickhandler, Trait.limbTraining1, Trait.tongueTraining1,
                                        Trait.powerfulhips, Trait.romantic, Trait.insatiable));
    }
}
