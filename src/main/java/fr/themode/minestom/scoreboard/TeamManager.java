package fr.themode.minestom.scoreboard;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TeamManager {

    // Represents all registered teams
    private Set<Team> teams = new CopyOnWriteArraySet<>();

    public Team createTeam(String teamName) {
        Team team = new Team(teamName);
        this.teams.add(team);
        return team;
    }

    public Set<Team> getTeams() {
        return teams;
    }
}
