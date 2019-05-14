package main.ImageRenderer;

import DAO.DaoImplementation;
import DAO.Entities.ArtistInfo;
import DAO.Entities.UrlCapsule;
import main.Exceptions.DiscogsServiceException;
import main.Youtube.DiscogsApi;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

public class UrlCapsuleConcurrentQueue extends LinkedBlockingQueue<UrlCapsule> {
	private DaoImplementation dao;
	private DiscogsApi discogsApi;

	private LinkedBlockingQueue<CompletableFuture<UrlCapsule>> wrapper;

	public UrlCapsuleConcurrentQueue(DaoImplementation dao, DiscogsApi discogsApi) {
		super();
		this.dao = dao;
		this.discogsApi = discogsApi;
		this.wrapper = new LinkedBlockingQueue<>();
	}

	@Override
	public int size() {
		return this.wrapper.size();
	}

	public boolean offer(@NotNull UrlCapsule name) {
		CompletableFuture<UrlCapsule> future = CompletableFuture.supplyAsync(() -> {
			name.setUrl(null);
			String url = dao.getArtistUrl(name.getArtistName());
			if (url == null) {
				try {
					url = discogsApi.findArtistImage(name.getArtistName());
					if (url != null) {
						System.out.println("Upserting buddy");
						dao.upsertUrl(new ArtistInfo(url, name.getArtistName()));
					}
				} catch (DiscogsServiceException e) {
					e.printStackTrace();
				}
			}
			name.setUrl(url);
			return name;
		}).toCompletableFuture();
		return wrapper.offer(future);
	}

	@NotNull
	public UrlCapsule take() throws InterruptedException {
		try {
			return wrapper.take().get();
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new InterruptedException();
		}
	}

}
