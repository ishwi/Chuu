package main.Commands;

import DAO.DaoImplementation;
import net.dv8tion.jda.client.events.group.GroupUserLeaveEvent;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.collections4.map.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AdministrativeCommand extends ListenerAdapter {
	private final DaoImplementation dao;

	public AdministrativeCommand(DaoImplementation dao) {
		this.dao = dao;
	}

	@Override
	public void onGroupUserLeave(GroupUserLeaveEvent event) {
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");

		Executors.newSingleThreadExecutor()
				.execute(() ->
						dao.remove(event.getUser().getIdLong())
				);
	}

	public void onStartup(JDA jda) {
		MultiValueMap<Long, Long> map = dao.getMapGuildUsers();
		//
		List<Long> usersIMightLikeToDelete = new ArrayList<>();
		List<Long> usersNotDeleted = new ArrayList<>();

		map.forEach((key, value) -> {
			List<Long> usersToDelete;
			List<Long> user = (List<Long>) map.getCollection(key);
			Guild guild = jda.getGuildById(key);
			if (guild != null) {
				List<Member> memberList = guild.getMembers();
				List<Long> guildList = memberList.stream().map(x -> x.getUser().getIdLong()).collect(Collectors.toList());
				usersToDelete = user.stream().filter(eachUser -> !guildList.contains(eachUser)).collect(Collectors.toList());
				usersNotDeleted.addAll(user.stream().filter(guildList::contains).collect(Collectors.toList()));

				//usersToDelete.forEach(dao::remove);
				usersIMightLikeToDelete.addAll(usersToDelete);
			} else {
				//When the bot is not presente on a guild check what users to delete;
				usersIMightLikeToDelete.addAll(user);

			}
		});
		for (Long potentiallyDeletedUser : usersIMightLikeToDelete) {
			if (!usersNotDeleted.contains(potentiallyDeletedUser))
				dao.remove(potentiallyDeletedUser);
		}


	}
}
