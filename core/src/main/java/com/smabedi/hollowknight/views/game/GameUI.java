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
import com.smabedi.hollowknight.config.AudioManager;
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

/**
 * Manages the Scene2D User Interface overlay during active gameplay.
 * Handles the instantiation, layout, and event delegation for the pause menu,
 * inventory system, NPC dialog boxes, and dynamic toast notifications.
 */
public class GameUI {
    public final Stage stage;
    private final Skin skin;
    private final Knight player;
    private final GameSession session;
    private final Inventory inventory;
    private Table pauseMenu;
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

    /**
     * Constructs the primary pause menu overlay, integrating session persistence
     * controls and developer cheat code references.
     */
    private void buildPauseMenu() {
        pauseMenu = new Table();
        pauseMenu.setFillParent(true);
        pauseMenu.defaults().pad(10);

        // Generate a semi-transparent dark overlay to contrast the menu against active gameplay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.85f));
        pixmap.fill();
        Texture transparentBlack = new Texture(pixmap);
        pauseMenu.setBackground(new TextureRegionDrawable(new TextureRegion(transparentBlack)));
        pixmap.dispose();

        Label title = new Label(Assets.getString("paused"), skin);
        title.setColor(Color.GOLD);
        pauseMenu.add(title).padBottom(20).row();

        TextButton continueBtn = new TextButton(Assets.getString("continue"), skin);
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });
        pauseMenu.add(continueBtn).width(200).row();

        TextButton settingsBtn = new TextButton(Assets.getString("settings"), skin);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.SETTINGS);
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
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

                ScreenManager.clearGameScreen();
                ScreenManager.setMenuScreen(ScreenType.MAIN);
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });
        pauseMenu.add(quitBtn).width(200).padBottom(30).row();

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

    /**
     * Initializes the dialog UI container used for NPC interactions.
     */
    private void buildDialogBox() {
        dialogBox = new Table();
        dialogBox.bottom().padBottom(50);
        dialogBox.setFillParent(true);

        dialogBox.setBackground(blackBackground);

        dialogTextLabel = new Label("", skin);
        dialogTextLabel.setWrap(true);
        dialogTextLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        dialogBox.add(dialogTextLabel).width(600).pad(20);
        dialogBox.setVisible(false);
        stage.addActor(dialogBox);
    }

    /**
     * Prepares the root container and background assets for the dynamic toast notification system.
     */
    private void buildToastSystem() {
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

    /**
     * Integrates dialogue into the UI with support for typewriter interrupts.
     * Invoking this while text is actively typing skips the animation. Invoking it
     * after typing is complete closes the dialogue box.
     */
    public void showDialog(String text) {
        if (isTyping) {
            displayedText = targetText;
            dialogTextLabel.setText(displayedText);
            isTyping = false;
            return;
        }

        if (dialogBox.isVisible() && !isTyping) {
            dialogBox.setVisible(false);
            return;
        }

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

    /**
     * Dispatches a transient notification to the screen corner.
     * Uses Scene2D Action sequencing for chained animation logic (Slide in -> Delay -> Slide out).
     */
    public void showToast(String message) {
        Table toastBox = new Table();

        Label toastLabel = new Label(message, skin);
        toastLabel.setColor(Color.GOLD);
        toastLabel.setAlignment(right);

        toastBox.add(toastLabel).align(right);
        toastBox.pack();

        // Encapsulate the toast within a WidgetGroup to enable independent animation sequencing
        // without disrupting the parent table's strict layout mechanics.
        WidgetGroup wrapper = new WidgetGroup();
        wrapper.setSize(toastBox.getWidth(), toastBox.getHeight());
        wrapper.addActor(toastBox);

        toastBox.setColor(1, 1, 1, 0);
        float slideOffset = 150f;
        toastBox.setPosition(slideOffset, 0);

        toastContainer.add(wrapper).size(toastBox.getWidth(), toastBox.getHeight()).padBottom(10).align(right).row();

        // Chain the presentation lifecycle
        toastBox.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(0.25f),
                Actions.moveTo(0, 0, 0.25f, pow2Out)
            ),
            Actions.delay(2f),
            Actions.parallel(
                Actions.fadeOut(0.5f),
                Actions.moveBy(slideOffset, 0, 0.5f, pow2In)
            ),
            Actions.run(() -> {
                // Execute layout invalidation to seamlessly collapse the gap left by the destroyed wrapper
                Cell<?> cell = toastContainer.getCell(wrapper);
                if (cell != null) {
                    cell.setActor(null);
                    cell.size(0, 0);
                    cell.pad(0);
                }
                toastContainer.invalidateHierarchy();

                wrapper.remove();
            })
        ));
    }

    /**
     * Constructs the inventory overlay layout, initializing the grid constraints
     * and binding logic for charm loadout management.
     */
    private void buildInventoryMenu() {
        inventoryMenu = new Table();
        inventoryMenu.setFillParent(true);
        inventoryMenu.defaults().pad(10);

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

    /**
     * Forces a localized data refresh of the inventory grid. Rebuilds the charm buttons
     * to accurately reflect equipped status and notch consumption logic.
     */
    @SuppressWarnings("GDXJavaUnsafeIterator")
    public void refreshInventoryUI() {
        notchLabel.setText(Assets.getString("notches_used") + ": " + inventory.getUsedNotches() + " / " + Constants.Knight.Inventory.MAX_NOTCHES);
        charmsGrid.clearChildren();

        int col = 0;
        for (CharmType charm : inventory.getOwnedCharms()) {
            boolean isEquipped = inventory.isEquipped(charm);

            String regionName = charm.getLangKey() + (isEquipped ? "" : "_deactive");
            TextureRegion charmIcon = Assets.getUiAtlas().findRegion(regionName);
            IconTextItem charmElement = new IconTextItem(charmIcon, charm.getName(), null, skin, true, !isEquipped);
            charmElement.setTouchable(Touchable.enabled);

            charmElement.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isEquipped) {
                        inventory.unequipCharm(charm);
                    } else {
                        inventory.equipCharm(charm);
                    }
                    charmDescription.setText(charm.getDescription());

                    refreshInventoryUI();
                    AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
                }
            });

            charmsGrid.add(charmElement).pad(15);
            col++;
            if (col >= 4) {
                col = 0;
                charmsGrid.row();
            }
        }
    }

    public void toggleInventory() {
        if (pauseMenu.isVisible()) return;

        boolean isVisible = inventoryMenu.isVisible();
        inventoryMenu.setVisible(!isVisible);

        if (!isVisible) {
            refreshInventoryUI();
            charmDescription.setText(Assets.getString("select_a_charm"));
        }
    }

    public boolean isInventoryOpen() {
        return inventoryMenu != null && inventoryMenu.isVisible();
    }

    /**
     * Advances dynamic UI logic, such as the dialogue typewriter effect, before drawing the stage.
     */
    public void render(float delta) {
        if (isTyping && dialogBox.isVisible()) {
            typewriterTimer += delta;
            if (typewriterTimer >= Constants.Zote.TYPE_SPEED) {
                typewriterTimer = 0f;
                displayedText += targetText.charAt(textIndex);
                dialogTextLabel.setText(displayedText);
                textIndex++;

                if (textIndex >= targetText.length()) {
                    isTyping = false;
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
