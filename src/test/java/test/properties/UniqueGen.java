package test.properties;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import dao.entities.ArtistPlays;
import dao.entities.UniqueWrapper;
import test.commands.utils.TestResources;

import java.util.List;

public class UniqueGen extends Generator<ArtistPlays> {
    private List<ArtistPlays> poolOfArtist;

    public UniqueGen() {

        super(ArtistPlays.class);
        long idLong = TestResources.channelWorker.getGuild().getIdLong();
        UniqueWrapper<ArtistPlays> wrapper = TestResources.dao.getUniqueArtist(idLong, "pablopita");
        poolOfArtist = wrapper.getUniqueData();
    }

    @Override
    public ArtistPlays generate(SourceOfRandomness random, GenerationStatus status) {
        return random.choose(poolOfArtist);
    }
}

