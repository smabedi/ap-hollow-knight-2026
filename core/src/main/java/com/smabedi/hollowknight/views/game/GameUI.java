package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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

import static com.badlogic.gdx.utils.Align.right;

public class GameUI {
    public final Stage stage;
    private final Skin skin;
    private final Inventory inventory;
    private Table pauseMenu;
    private Table dialogBox;
    private Table toastContainer;
    private TextureRegionDrawable toastBackground;
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
        buildToastSystem();
        buildDialogBox();
        buildInventoryMenu();
    }

    private void buildPauseMenu() {
        pauseMenu = new Table();
        pauseMenu.setFillParent(true);
        pauseMenu.defaults().pad(10);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.6f));
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
        // Create a reusable, dark, semi-transparent background for the toast boxes
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.6f));
        pixmap.fill();
        Texture bgTex = new Texture(pixmap);
        toastBackground = new TextureRegionDrawable(new TextureRegion(bgTex));
        pixmap.dispose();

        toastContainer = new Table();
        // Anchor to the Upper Right corner!
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
        // 1. Create a dedicated box for this specific toast
        Table toastBox = new Table();
        toastBox.setBackground(toastBackground);
        toastBox.pad(15); // Inner padding so text doesn't touch the edges

        // 2. Setup the text label
        Label toastLabel = new Label(message, skin);
        toastLabel.setWrap(true); // Prevents long text from breaking the layout
        toastLabel.setAlignment(right); // Text is right-aligned inside the box

        // 3. Add the label to the box and lock its width
        toastBox.add(toastLabel).width(250).align(right);

        // 4. Start the box as completely transparent
        toastBox.setColor(1, 1, 1, 0);

        // 5. Add the box to the main Upper-Left container.
        // Aligning right here ensures all boxes stack neatly against their own column's right edge.
        toastContainer.add(toastBox).width(280).padBottom(10).align(right).row();

        // 6. Smooth Animation Sequence
        toastBox.addAction(Actions.sequence(
            Actions.fadeIn(0.25f),
            Actions.delay(2f),
            Actions.fadeOut(0.5f),
            Actions.run(() -> {
                // Safely extract the cell and reset it to collapse the gap
                com.badlogic.gdx.scenes.scene2d.ui.Cell<?> cell = toastContainer.getCell(toastBox);
                if (cell != null) {
                    cell.reset();
                }
                // Destroy the box
                toastBox.remove();
            })
        ));
    }

    private void buildInventoryMenu() {
        inventoryMenu = new Table();
        inventoryMenu.setFillParent(true);
        inventoryMenu.defaults().pad(10);

        // Reuse the translucent black background from the pause menu
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.6f));
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
