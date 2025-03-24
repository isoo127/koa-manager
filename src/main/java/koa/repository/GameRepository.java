package koa.repository;

import koa.model.tournament.Game;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GameRepository implements Repository<Game>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FILE_PATH = "db/game.lib";
    private long lastId = 0;
    private HashMap<Long, Game> games;

    private GameRepository() {
        games = new HashMap<>();
        load();
    }

    private static final class InstanceHolder {
        private static final GameRepository instance = new GameRepository();
    }

    public static GameRepository getInstance() {
        return GameRepository.InstanceHolder.instance;
    }

    private void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            GameRepository loaded = (GameRepository) ois.readObject();
            this.games = loaded.games;
            this.lastId = loaded.lastId;
            System.out.println("GameRepository loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading GameRepository from file", e);
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
            throw new RuntimeException("Error saving GameRepository to file", e);
        }
    }

    @Override
    public Game findById(long id) {
        return games.get(id);
    }

    @Override
    public Collection<Game> getAll() {
        return games.values();
    }

    @Override
    public void insert(Game element) {
        element.setId(++lastId);
        games.put(element.getId(), element);
    }

    @Override
    public void delete(long id) {
        games.remove(id);
    }

}
