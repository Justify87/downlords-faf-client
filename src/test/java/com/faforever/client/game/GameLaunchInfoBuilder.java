package com.faforever.client.game;

import com.faforever.client.legacy.domain.GameLaunchMessage;

import java.util.Arrays;

public class GameLaunchInfoBuilder {

  private final GameLaunchMessage gameLaunchMessage;

  public GameLaunchInfoBuilder() {
    gameLaunchMessage = new GameLaunchMessage();
  }

  public GameLaunchInfoBuilder defaultValues() {
    gameLaunchMessage.setUid(1);
    gameLaunchMessage.setMod("mod");
    gameLaunchMessage.setArgs(Arrays.asList("/ratingcolor red", "/clan foo"));
    return this;
  }

  public GameLaunchMessage get() {
    return gameLaunchMessage;
  }

  public static GameLaunchInfoBuilder create() {
    return new GameLaunchInfoBuilder();
  }
}
