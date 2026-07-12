package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.repositories.DatabaseManager;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.models.inventory.CharmType;
import com.smabedi.hollowknight.models.inventory.Inventory;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;
import com.smabedi.hollowknight.views.customelements.IconTextItem;

import static com.badlogic.gdx.math.Interpolation.pow2In;
import static com.badlogic.gdx.math.Interpolation.pow2Out;
import static com.badlogic.gdx.utils.Align.right;

public class GameUI {
    public final Stage stage;
    private final Skin skin;
    private Table pauseMenu;
    private final Knight player;
    private final GameSession session;
    private final Inventory inventory;
    private Table inventoryMenu;
    private Label notchLabel;
    private Label charmDescription;
    private Table charmsGrid;
    private Table toastContainer;
    private TextureRegionDrawable blackBackground;
    private Table dialogBox;
    private Label dialogTextLabel;
    private String targetText = "";
    private String displayedText = "";
    private float typewriterTimer = 0f;
    private int textIndex = 0;
    private boolean isTyping = false;

    public GameUI(GameSession session, Knight player) {
        this.player = player;
        this.session = session;
        this.inventory = session.inventory;
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
        pixmap.setColor(new Color(0, 0, 0, 0.85f)); // Darkened slightly for better readability
        pixmap.fill();
        Texture transparentBlack = new Texture(pixmap);
        pauseMenu.setBackground(new TextureRegionDrawable(new TextureRegion(transparentBlack)));
        pixmap.dispose();

        Label title = new Label(Assets.getString("paused"), skin);
        title.setColor(Color.GOLD);
        pauseMenu.add(title).padBottom(20).row();

        // --- BUTTONS ---
        TextButton continueBtn = new TextButton(Assets.getString("continue"), skin);
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
            }
        });
        pauseMenu.add(continueBtn).width(200).row();

        TextButton settingsBtn = new TextButton(Assets.getString("settings"), skin);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // SettingsMenuScreen will lay over the game.
                // We don't dispose the game screen here, just switch contexts.
                ScreenManager.setMenuScreen(ScreenType.SETTINGS);
            }
        });
        pauseMenu.add(settingsBtn).width(200).row();

        TextButton quitBtn = new TextButton(Assets.getString("save_and_quit"), skin);
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                session.health = player.health;
                session.soul = player.soul;
                session.playerX = player.b2body.getPosition().x;
                session.playerY = player.b2body.getPosition().y;

                DatabaseManager.saveSession(session);

                // Clear the active game before returning to the main menu!
                ScreenManager.clearGameScreen();
                ScreenManager.setMenuScreen(ScreenType.MAIN);
            }
        });
        pauseMenu.add(quitBtn).width(200).padBottom(30).row();

        // --- CHEAT CODES DISPLAY ---
        Table cheatsTable = new Table();
        cheatsTable.defaults().pad(5).left();

        Label cheatsTitle = new Label("--- " + Assets.getString("cheat_codes") + " ---", skin);
        cheatsTitle.setColor(Color.GOLD);
        cheatsTable.add(cheatsTitle).center().padTop(50).padBottom(10).row();

        cheatsTable.add(new Label(Assets.getString("guide_cheat_boss") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.BOSS_TELEPORT), skin)).row();
        cheatsTable.add(new Label(Assets.getString("guide_cheat_noclip") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.SPECTATOR_MODE), skin)).row();
        cheatsTable.add(new Label(Assets.getString("guide_cheat_heal") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.EMERGENCY_HEAL), skin)).row();
        cheatsTable.add(new Label(Assets.getString("guide_cheat_soul") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.REFILL_SOUL), skin)).row();
        cheatsTable.add(new Label(Assets.getString("guide_cheat_god") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.GOD_MODE), skin)).row();
        cheatsTable.add(new Label(Assets.getString("guide_cheat_dilation") + ": "
            + Input.Keys.toString(Constants.Cheats.Keys.MODIFIER) + " + "
            + Input.Keys.toString(Constants.Cheats.Keys.TIME_DILATION), skin)).row();

        pauseMenu.add(cheatsTable).height(150).width(350).row();

        pauseMenu.setVisible(false);
        stage.addActor(pauseMenu);
    }

    private void buildDialogBox() {
        dialogBox = new Table();
        dialogBox.bottom().padBottom(50);
        dialogBox.setFillParent(true);

        // Dark background for readability
        dialogBox.setBackground(blackBackground); // Reusing the toast background

        dialogTextLabel = new Label("", skin);
        dialogTextLabel.setWrap(true);
        dialogTextLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        dialogBox.add(dialogTextLabel).width(600).pad(20);
        dialogBox.setVisible(false);
        stage.addActor(dialogBox);
    }

    private void buildToastSystem() {
        // Create a reusable, dark, semi-transparent background for the toast boxes
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.6f));
        pixmap.fill();
        Texture bgTex = new Texture(pixmap);
        blackBackground = new TextureRegionDrawable(new TextureRegion(bgTex));
        pixmap.dispose();

        toastContainer = new Table();
        toastContainer.top().right().pad(20);
        toastContainer.setFillParent(true);
        stage.addActor(toastContainer);
    }

    public void togglePause() {
        boolean isPaused = pauseMenu.isVisible();
        pauseMenu.setVisible(!isPaused);
    }

    public void showDialog(String text) {
        if (isTyping) {
            // Player pressed UP while typing: Skip to the end!
            displayedText = targetText;
            dialogTextLabel.setText(displayedText);
            isTyping = false;
            return;
        }

        if (dialogBox.isVisible() && !isTyping) {
            // Player pressed UP after text finished: Close the box
            dialogBox.setVisible(false);
            return;
        }

        // Start a new dialogue line
        targetText = text;
        displayedText = "";
        dialogTextLabel.setText("");
        textIndex = 0;
        isTyping = true;
        dialogBox.setVisible(true);
    }

    public boolean isDialogVisible() {
        return dialogBox.isVisible();
    }

    public void showToast(String message) {
        // 1. Create the core toast box
        Table toastBox = new Table();

        Label toastLabel = new Label(message, skin);
        toastLabel.setColor(Color.GOLD);
        toastLabel.setAlignment(right);

        toastBox.add(toastLabel).align(right);
        toastBox.pack(); // Calculate the box size based on the text

        // 2. THE WRAPPER TRICK
        // A WidgetGroup reserves space in the parent Table, but doesn't force layout on its children.
        // This allows us to animate the toastBox's position inside it!
        WidgetGroup wrapper = new WidgetGroup();
        wrapper.setSize(toastBox.getWidth(), toastBox.getHeight());
        wrapper.addActor(toastBox);

        // 3. Set starting state: invisible and pushed 150 pixels to the right
        toastBox.setColor(1, 1, 1, 0);
        float slideOffset = 150f;
        toastBox.setPosition(slideOffset, 0);

        // 4. Add the WRAPPER to the main container (not the toastBox directly)
        toastContainer.add(wrapper).size(toastBox.getWidth(), toastBox.getHeight()).padBottom(10).align(right).row();

        // 5. Smooth Slide & Fade Sequence
        toastBox.addAction(Actions.sequence(
            // IN: Slide left to (0,0) and fade in at the same time
            Actions.parallel(
                Actions.fadeIn(0.25f),
                Actions.moveTo(0, 0, 0.25f, pow2Out) // pow2Out gives a natural decelerating slide
            ),
            Actions.delay(2f),
            // OUT: Slide back out to the right and fade out
            Actions.parallel(
                Actions.fadeOut(0.5f),
                Actions.moveBy(slideOffset, 0, 0.5f, pow2In)
            ),
            Actions.run(() -> {
                // Safely collapse the gap without breaking the Table's row logic
                Cell<?> cell = toastContainer.getCell(wrapper);
                if (cell != null) {
                    cell.setActor(null); // Remove the wrapper from the cell
                    cell.size(0, 0);     // Shrink the cell to 0 width/height
                    cell.pad(0);         // Remove the padding
                }
                // Force the table to recalculate layout to close the gap
                toastContainer.invalidateHierarchy();

                // Destroy the wrapper (which destroys the toastBox)
                wrapper.remove();
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
        title.setColor(Color.GOLD);
        inventoryMenu.add(title).row();

        notchLabel = new Label("", skin);
        inventoryMenu.add(notchLabel).padBottom(20).row();

        charmsGrid = new Table();
        inventoryMenu.add(charmsGrid).expandX().fillX().row();

        charmDescription = new Label(Assets.getString("select_a_charm"), skin, "small");
        charmDescription.setWrap(true);
        charmDescription.setAlignment(Align.center);
        inventoryMenu.add(charmDescription).width(800).padTop(30);

        inventoryMenu.setVisible(false);
        stage.addActor(inventoryMenu);
    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    public void refreshInventoryUI() {
        notchLabel.setText(Assets.getString("notches_used") + ": " + inventory.getUsedNotches() + " / " + Constants.Knight.Inventory.MAX_NOTCHES);
        charmsGrid.clearChildren();

        int col = 0;
        for (CharmType charm : inventory.getOwnedCharms()) {
            boolean isEquipped = inventory.isEquipped(charm);

            // Determine the exact region name based on the equipped state
            String regionName = charm.getLangKey() + (isEquipped ? "" : "_deactive");

            // Fetch the correct icon from the HUD atlas
            TextureRegion charmIcon = Assets.getUiAtlas().findRegion(regionName);

            // Instantiate the custom actor with vertical layout (icon on top, name below)
            // We pass null for the description so it doesn't render text next to it
            IconTextItem charmElement = new IconTextItem(charmIcon, charm.getName(), null, skin, true, !isEquipped);

            // Ensure the Table catches the click events
            charmElement.setTouchable(Touchable.enabled);

            charmElement.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isEquipped) {
                        inventory.unequipCharm(charm);
                    } else {
                        inventory.equipCharm(charm);
                    }
                    // Update the dedicated description label located elsewhere in your UI
                    charmDescription.setText(charm.getDescription());

                    // Rebuild the grid to instantly refresh the icon textures
                    refreshInventoryUI();
                }
            });

            charmsGrid.add(charmElement).pad(15);
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
        if (isTyping && dialogBox.isVisible()) {
            typewriterTimer += delta;
            if (typewriterTimer >= Constants.Zote.TYPE_SPEED) {
                typewriterTimer = 0f;
                displayedText += targetText.charAt(textIndex);
                dialogTextLabel.setText(displayedText);
                textIndex++;

                if (textIndex >= targetText.length()) {
                    isTyping = false; // Finished typing
                }
            }
        }

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
