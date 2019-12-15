package test.properties;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import dao.entities.UniqueData;
import dao.entities.UniqueWrapper;
import test.commands.utils.TestResources;

import java.util.List;

public class UniqueGen extends Generator<UniqueData> {
	private List<UniqueData> poolOfArtist;

	public UniqueGen() {

		super(UniqueData.class);
		long idLong = TestResources.channelWorker.getGuild().getIdLong();
		UniqueWrapper<UniqueData> wrapper = TestResources.dao.getUniqueArtist(idLong, "pablopita");
		poolOfArtist = wrapper.getUniqueData();
	}

	@Override
	public UniqueData generate(SourceOfRandomness random, GenerationStatus status) {
		return random.choose(poolOfArtist);
	}
}

