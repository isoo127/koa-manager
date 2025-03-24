package koa.repository;

import koa.model.player.Player;
import java.io.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PlayerRepository implements Repository<Player>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FILE_PATH = "db/player.lib";
    private long lastId = 0;
    private HashMap<Long, Player> players;

    private PlayerRepository() {
        players = new HashMap<>();
        load();
    }

    private static final class InstanceHolder {
        private static final PlayerRepository instance = new PlayerRepository();
    }

    public static PlayerRepository getInstance() {
        return InstanceHolder.instance;
    }

    private void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            PlayerRepository loaded = (PlayerRepository) ois.readObject();
            this.players = loaded.players;
            this.lastId = loaded.lastId;
            System.out.println("PlayerRepository loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading PlayerRepository from file", e);
        }
    }

    @Override
    public void save() {
        File file = new File(FILE_PATH);

        if (!file.getParentFile().exists()) {
            boolean dirsCreated = file.getParentFile().mkdirs();
            if (!dirsCreated) {
                throw new RuntimeException("Failed to create directories: " + file.getParentFile());
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException("Error saving PlayerRepository to file", e);
        }
    }

    @Override
    public Player findById(long id) {
        return players.get(id);
    }

    @Override
    public Collection<Player> getAll() {
        return players.values();
    }

    @Override
    public void insert(Player element) {
        element.setId(++lastId);
        players.put(element.getId(), element);
    }

    @Override
    public void delete(long id) {
        players.remove(id);
    }

    public List<Player> getSortedPlayers() {
        Collection<Player> players = getAll();
        return players.stream()
                .sorted(Comparator.comparing(Player::getRank).reversed())
                .toList();
    }

}
