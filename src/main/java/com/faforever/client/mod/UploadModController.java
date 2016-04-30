package com.faforever.client.mod;

import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.Action;
import com.faforever.client.notification.DismissAction;
import com.faforever.client.notification.ImmediateNotification;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.notification.ReportAction;
import com.faforever.client.notification.Severity;
import com.faforever.client.reporting.ReportingService;
import com.faforever.client.util.IdenticonUtil;
import javafx.beans.binding.Bindings;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.Arrays.asList;

public class UploadModController {

  @FXML
  Pane parseProgressPane;
  @FXML
  Pane uploadProgressPane;
  @FXML
  Pane uploadCompletePane;
  @FXML
  ProgressBar uploadProgressBar;
  @FXML
  Pane modInfoPane;
  @FXML
  Label modNameLabel;
  @FXML
  Label descriptionLabel;
  @FXML
  Label versionLabel;
  @FXML
  Label uidLabel;
  @FXML
  ImageView thumbnailImageView;
  @FXML
  Region modUploadRoot;

  @Resource
  ModService modService;
  @Resource
  ThreadPoolExecutor threadPoolExecutor;
  @Resource
  NotificationService notificationService;
  @Resource
  ReportingService reportingService;
  @Resource
  I18n i18n;

  private Path modPath;
  private UploadModTask uploadModTask;

  @FXML
  void initialize() {
    modInfoPane.managedProperty().bind(modInfoPane.visibleProperty());
    uploadProgressPane.managedProperty().bind(uploadProgressPane.visibleProperty());
    parseProgressPane.managedProperty().bind(parseProgressPane.visibleProperty());
    uploadCompletePane.managedProperty().bind(uploadCompletePane.visibleProperty());

    modInfoPane.setVisible(false);
    uploadProgressPane.setVisible(false);
    parseProgressPane.setVisible(false);
    uploadCompletePane.setVisible(false);
  }

  public void setModPath(Path modPath) {
    this.modPath = modPath;
    enterParsingState();
    CompletableFuture.supplyAsync(() -> modService.extractModInfo(modPath), threadPoolExecutor)
        .thenAccept(this::setModInfo);
  }

  private void enterParsingState() {
    modInfoPane.setVisible(false);
    uploadProgressPane.setVisible(false);
    parseProgressPane.setVisible(true);
    uploadCompletePane.setVisible(false);
  }

  private void setModInfo(ModInfoBean modInfo) {
    enterModInfoState();
    modNameLabel.textProperty().bind(modInfo.nameProperty());
    descriptionLabel.textProperty().bind(modInfo.descriptionProperty());
    versionLabel.textProperty().bind(modInfo.versionProperty());
    uidLabel.textProperty().bind(modInfo.idProperty());
    thumbnailImageView.imageProperty().bind(
        Bindings.createObjectBinding(() -> {
          if (modInfo.getImagePath() != null && Files.isRegularFile(modInfo.getImagePath())) {
            return new Image(modPath.resolve(modInfo.getImagePath()).toString());
          }

          return IdenticonUtil.createIdenticon(modInfo.getId());
        }, modInfo.idProperty(), modInfo.imagePathProperty())
    );
  }

  private void enterModInfoState() {
    modInfoPane.setVisible(true);
    uploadProgressPane.setVisible(false);
    parseProgressPane.setVisible(false);
    uploadCompletePane.setVisible(false);
  }

  @FXML
  void onCancelUploadClicked() {
    uploadModTask.cancel(true);
    enterModInfoState();
  }

  private void onUploadFailed(WorkerStateEvent workerStateEvent) {
    Throwable throwable = workerStateEvent.getSource().getException();
    notificationService.addNotification(new ImmediateNotification(
        i18n.get("errorTitle"),
        i18n.get("modVault.upload.failed"),
        Severity.ERROR,
        throwable,
        asList(
            new Action(i18n.get("modVault.upload.retry"), event -> onUploadClicked()),
            new ReportAction(i18n, reportingService, throwable),
            new DismissAction(i18n)
        )
    ));
  }

  @FXML
  void onUploadClicked() {
    enterUploadingState();
    uploadProgressBar.setProgress(0);
    uploadProgressPane.setVisible(true);
    uploadModTask = modService.uploadMod(modPath,
        progress -> uploadProgressBar.setProgress(progress),
        event -> enterUploadCompleteState(),
        this::onUploadFailed
    );
  }

  private void enterUploadingState() {
    modInfoPane.setVisible(false);
    uploadProgressPane.setVisible(true);
    parseProgressPane.setVisible(false);
    uploadCompletePane.setVisible(false);
  }

  private void enterUploadCompleteState() {
    modInfoPane.setVisible(false);
    uploadProgressPane.setVisible(false);
    parseProgressPane.setVisible(false);
    uploadCompletePane.setVisible(true);
  }

  @FXML
  void onCancelClicked() {
    getRoot().getScene().getWindow().hide();
  }

  public Region getRoot() {
    return modUploadRoot;
  }
}
