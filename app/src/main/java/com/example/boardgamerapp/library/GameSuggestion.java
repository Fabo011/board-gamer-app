package com.example.boardgamerapp.library;


import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

    /**
     * Datenmodell für Spielvorschläge mit Firestore-Annotationen
     */
    public class GameSuggestion {
        @DocumentId
        private String id;

        @PropertyName("game_name")
        private String gameName;

        @PropertyName("suggested_by")
        private String suggestedBy;

        @PropertyName("player_count_min")
        private int playerCountMin;

        @PropertyName("player_count_max")
        private int playerCountMax;

        @PropertyName("game_description")
        private String gameDescription;

        @PropertyName("votes")
        private int votes;

        @PropertyName("is_selected")
        private boolean isSelected;

        @ServerTimestamp
        private Date createdAt;

        // Standardkonstruktor für Firestore
        public GameSuggestion() {}

        // Konstruktor mit allen Parametern
        public GameSuggestion(String gameName, String suggestedBy,
                              int playerCountMin, int playerCountMax,
                              String gameDescription) {
            this.gameName = gameName;
            this.suggestedBy = suggestedBy;
            this.playerCountMin = playerCountMin;
            this.playerCountMax = playerCountMax;
            this.gameDescription = gameDescription;
            this.votes = 0;
            this.isSelected = false;
        }

        // Getter und Setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGameName() {
            return gameName;
        }

        public void setGameName(String gameName) {
            this.gameName = gameName;
        }

        public String getSuggestedBy() {
            return suggestedBy;
        }

        public void setSuggestedBy(String suggestedBy) {
            this.suggestedBy = suggestedBy;
        }

        public int getPlayerCountMin() {
            return playerCountMin;
        }

        public void setPlayerCountMin(int playerCountMin) {
            this.playerCountMin = playerCountMin;
        }

        public int getPlayerCountMax() {
            return playerCountMax;
        }

        public void setPlayerCountMax(int playerCountMax) {
            this.playerCountMax = playerCountMax;
        }

        public String getGameDescription() {
            return gameDescription;
        }

        public void setGameDescription(String gameDescription) {
            this.gameDescription = gameDescription;
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        // Methode zum Inkrementieren der Stimmen
        public void incrementVotes() {
            this.votes++;
        }
    }

