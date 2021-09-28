package core.services;

import java.util.Set;

public record MessageDeletionService(Set<Long> serversToDelete) {


    public void addServerToDelete(long id) {
        this.serversToDelete.add(id);
    }

    public void removeServerToDelete(long id) {
        this.serversToDelete.remove(id);
    }

    public boolean isMarked(long id) {
        return this.serversToDelete.contains(id);
    }
}
