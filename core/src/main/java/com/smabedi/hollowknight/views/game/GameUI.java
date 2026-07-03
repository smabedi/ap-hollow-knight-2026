package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.inventory.CharmType;
import com.smabedi.hollowknight.models.inventory.Inventory;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;

public class GameUI {
    public final Stage stage;
    private final Skin skin;
    private final Inventory inventory;
    private Table pauseMenu;
    private Table dialogBox;
    private Table toastContainer;
    private Table inventoryMenu;
    private Label notchLabel;
    private Label charmDescription;
    private Table charmsGrid;

    public GameUI(Inventory inventory) {
        this.inventory = inventory;
        ScreenViewport viewport = new ScreenViewport(new OrthographicCamera());
        viewport.setUnitsPerPixel(1f / Constants.UI.UPP);
        stage = new Stage(viewport);
        skin = Assets.getSkin();

        buildPauseMenu();
        buildInventoryMenu();
    }

    private void buildPauseMenu() {
        pauseMenu = new Table();
        pauseMenu.setFillParent(true);
        pauseMenu.defaults().pad(10);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.5f));
        pixmap.fill();
        Texture transparentBlack = new Texture(pixmap);
        pauseMenu.setBackground(new TextureRegionDrawable(new TextureRegion(transparentBlack)));
        pixmap.dispose();

        Label title = new Label(Assets.getString("paused"), skin);
        pauseMenu.add(title).row();

        TextButton backBtn = new TextButton(Assets.getString("back"), skin);
        pauseMenu.add(backBtn).row();

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
            }
        });

        // TODO: Add Resume, Settings, Quit buttons here.

        pauseMenu.setVisible(false);
        stage.addActor(pauseMenu);
    }

    private void buildDialogBox() {
        dialogBox = new Table();
        dialogBox.bottom().padBottom(50);
        dialogBox.setFillParent(true);

        // TODO: Add text labels her.

        dialogBox.setVisible(false);
        stage.addActor(dialogBox);
    }

    private void buildToastSystem() {
        toastContainer = new Table();
        toastContainer.top().right().pad(20);
        toastContainer.setFillParent(true);
        stage.addActor(toastContainer);
    }


    public void togglePause() {
        boolean isPaused = pauseMenu.isVisible();
        pauseMenu.setVisible(!isPaused);
        // TODO: Tell the PlayScreen's update() loop to stop stepping the world here.
    }

    public void showDialog(String text) {
        // TODO: Update label text and setVisible(true).
    }

    public void showToast(String message) {
        // TODO: Add a label to toastContainer, use Scene2D Actions to fade it out after 3 seconds.
    }

    private void buildInventoryMenu() {
        inventoryMenu = new Table();
        inventoryMenu.setFillParent(true);
        inventoryMenu.defaults().pad(10);

        // Reuse the translucent black background from the pause menu
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.8f));
        pixmap.fill();
        Texture transparentBlack = new Texture(pixmap);
        inventoryMenu.setBackground(new TextureRegionDrawable(new TextureRegion(transparentBlack)));
        pixmap.dispose();

        Label title = new Label(Assets.getString("inventory"), skin);
        inventoryMenu.add(title).row();

        notchLabel = new Label("", skin);
        inventoryMenu.add(notchLabel).padBottom(20).row();

        charmsGrid = new Table();
        inventoryMenu.add(charmsGrid).expandX().fillX().row();

        charmDescription = new Label(Assets.getString("select_a_charm"), skin);
        charmDescription.setWrap(true);
        charmDescription.setAlignment(com.badlogic.gdx.utils.Align.center);
        inventoryMenu.add(charmDescription).width(800).padTop(30);

        inventoryMenu.setVisible(false);
        stage.addActor(inventoryMenu);
    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    public void refreshInventoryUI() {
        notchLabel.setText(Assets.getString("notches_used") + ": " + inventory.getUsedNotches() + " / " + Constants.Inventory.MAX_NOTCHES);
        charmsGrid.clearChildren();

        int col = 0;
        for (CharmType charm : inventory.getOwnedCharms()) {
            boolean isEquipped = inventory.isEquipped(charm);

            // Append (EQ) if equipped for basic visual feedback
            String btnText = charm.getName() + (isEquipped ? " (EQ)" : "");
            TextButton charmBtn = new TextButton(btnText, skin);

            if (isEquipped) {
                charmBtn.setColor(Color.LIME); // Highlight equipped charms
            }

            charmBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isEquipped) {
                        inventory.unequipCharm(charm);
                    } else {
                        inventory.equipCharm(charm);
                    }
                    charmDescription.setText(charm.getDescription());
                    refreshInventoryUI(); // Rebuild the grid to update colors/text
                }
            });

            charmsGrid.add(charmBtn).width(200).height(50).pad(10);
            col++;
            if (col >= 4) { // 4 charms per row
                col = 0;
                charmsGrid.row();
            }
        }
    }

    public void toggleInventory() {
        if (pauseMenu.isVisible()) return; // Don't open if standard pause menu is up

        boolean isVisible = inventoryMenu.isVisible();
        inventoryMenu.setVisible(!isVisible);

        if (!isVisible) {
            refreshInventoryUI(); // Refresh data every time we open it
            charmDescription.setText(Assets.getString("select_a_charm"));
        }
    }

    public boolean isInventoryOpen() {
        return inventoryMenu != null && inventoryMenu.isVisible();
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    public boolean isPaused() {
        return (pauseMenu != null && pauseMenu.isVisible()) || (inventoryMenu != null && inventoryMenu.isVisible());
    }
}
