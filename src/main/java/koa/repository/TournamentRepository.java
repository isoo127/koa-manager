package koa.repository;

import koa.model.tournament.Tournament;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

public class TournamentRepository implements Repository<Tournament>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FILE_PATH = "db/tournament.lib";
    private long lastId = 0;
    private HashMap<Long, Tournament> tournaments;

    private TournamentRepository() {
        tournaments = new HashMap<>();
        load();
    }

    private static final class InstanceHolder {
        private static final TournamentRepository instance = new TournamentRepository();
    }

    public static TournamentRepository getInstance() {
        return TournamentRepository.InstanceHolder.instance;
    }

    private void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            TournamentRepository loaded = (TournamentRepository) ois.readObject();
            this.tournaments = loaded.tournaments;
            this.lastId = loaded.lastId;
            System.out.println("TournamentRepository loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading TournamentRepository from file", e);
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
            throw new RuntimeException("Error saving TournamentRepository to file", e);
        }
    }

    @Override
    public Tournament findById(long id) {
        return tournaments.get(id);
    }

    @Override
    public Collection<Tournament> getAll() {
        return tournaments.values();
    }

    @Override
    public void insert(Tournament element) {
        element.setId(++lastId);
        tournaments.put(element.getId(), element);
    }

    @Override
    public void delete(long id) {
        tournaments.remove(id);
    }

}
