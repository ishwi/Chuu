package core.services;

import java.util.Set;

public class MessageDeletionService {
    private final Set<Long> serversToDelete;

    public MessageDeletionService(Set<Long> serversToDelete) {
        this.serversToDelete = serversToDelete;
    }

    public Set<Long> getServersToDelete() {
        return serversToDelete;
    }

    public void addServerToDelete(long id) {
        this.serversToDelete.add(id);
    }

    public boolean removeServerToDelete(long id) {
        return this.serversToDelete.remove(id);
    }

    public boolean isMarked(long id) {
        return this.serversToDelete.contains(id);
    }
}
