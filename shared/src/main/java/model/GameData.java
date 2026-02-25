package model;

import chess.ChessGame;

public record GameData(int gameID, String teamAUsername, String teamBUsername, String gameName, ChessGame game) {}
