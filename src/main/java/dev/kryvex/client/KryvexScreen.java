package dev.kryvex.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.*;

public final class KryvexScreen extends Screen {
    private enum Tab { VISUALS, HUD, FRIENDS, COSMETICS, TOOLS }
    private Tab tab = Tab.VISUALS;
    private TextFieldWidget friendInput;
    private final Config c = KryvexClient.CONFIG;
    private int panelX, panelY, panelW, panelH;

    public KryvexScreen() { super(Text.literal("Kryvex Client")); }
    @Override protected void init() {
        clearChildren(); hints.clear();
        panelW = Math.min(620, width - 24); panelH = Math.min(360, height - 24);
        panelX = (width-panelW)/2; panelY=(height-panelH)/2;
        int sideX=panelX+14, top=panelY+58;
        String[] names={"Визуалы","HUD","Друзья","Косметика","Инструменты"};
        Tab[] tabs=Tab.values();
        for(int i=0;i<tabs.length;i++) { final Tab t=tabs[i]; addDrawableChild(ButtonWidget.builder(Text.literal((tab==t?"§b● §f":"§7")+names[i]), b->{tab=t;init();}).dimensions(sideX,top+i* thirty(),142,25).build()); }
        int contentX=panelX+174, contentW=panelW-190;
        if(tab==Tab.FRIENDS) buildFriends(contentX,top,contentW);
        else buildModules(contentX,top,contentW);
        addDrawableChild(ButtonWidget.builder(Text.literal("Сохранить"),b->{c.save();}).dimensions(panelX+panelW-112,panelY+panelH-34,96,22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"),b->close()).dimensions(panelX+panelW-214,panelY+panelH-34,96,22).build());
    }
    private int thirty(){return 31;}
    private void buildModules(int x,int y,int w) {
        switch(tab) {
            case VISUALS -> {
                module(x,y,w,"Время: Утро",()->setTime(1000)); module(x,y+34,w,"Время: День",()->setTime(6000));
                module(x,y+68,w,"Время: Закат",()->setTime(12000)); module(x,y+102,w,"Время: Ночь",()->setTime(18000));
                drawHint(x,y+148,"Время применяется локально; сервер может его переопределить.");
            }
            case HUD -> {
                toggle(x,y,w,"Водяной знак Kryvex",c.watermark,v->c.watermark=v);
                toggle(x,y+34,w,"Счётчик FPS",c.fpsHud,v->c.fpsHud=v);
                toggle(x,y+68,w,"Координаты XYZ",c.coordsHud,v->c.coordsHud=v);
                toggle(x,y+102,w,"Игровое время",c.clockHud,v->c.clockHud=v);
                toggle(x,y+136,w,"Кастомный прицел",c.customCrosshair,v->c.customCrosshair=v);
            }
            case COSMETICS -> {
                toggle(x,y,w,"Тег ✦ KRYVEX ✦ на экране",c.cosmeticTag,v->c.cosmeticTag=v);
                addDrawableChild(ButtonWidget.builder(Text.literal("Тема: CYAN / DARK"),b->{c.theme=c.theme.equals("CYAN")?"VIOLET":"CYAN";}).dimensions(x,y+42,w,25).build());
                drawHint(x,y+82,"Локальная косметика интерфейса. Не меняет скин и не требует сервера.");
            }
            case TOOLS -> {
                toggle(x,y,w,"Fullbright (яркость 100%)",c.fullbright,v->{c.fullbright=v;MinecraftClient mc=MinecraftClient.getInstance();mc.options.getGamma().setValue(v?16.0:1.0);});
                toggle(x,y+34,w,"Минимум частиц (FPS)",c.reduceParticles,v->{c.reduceParticles=v;MinecraftClient mc=MinecraftClient.getInstance();mc.options.getParticles().setValue(v?net.minecraft.particle.ParticlesMode.MINIMAL:net.minecraft.particle.ParticlesMode.ALL);});
                toggle(x,y+68,w,"Toggle Sprint",c.autoSprint,v->c.autoSprint=v);
                toggle(x,y+102,w,"Anti-AFK напоминание",c.antiAfk,v->c.antiAfk=v);
                drawHint(x,y+148,"Оптимизация меняет только клиентские настройки графики.");
            }
            default -> { }
        }
    }
    private void buildFriends(int x,int y,int w) {
        drawHint(x,y,"Список друзей хранится локально на этом компьютере.");
        friendInput=new TextFieldWidget(textRenderer,x,y+32,w-84,24,Text.literal("Ник игрока")); friendInput.setMaxLength(16); friendInput.setPlaceholder(Text.literal("Введите ник...")); addDrawableChild(friendInput);
        addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"),b->{String n=friendInput.getText().trim();if(!n.isEmpty()&&!c.friends.contains(n)){c.friends.add(n);c.save();friendInput.setText("");init();}}).dimensions(x+w-78,y+32,78,24).build());
        int yy=y+70;
        for(String name:new ArrayList<>(c.friends).subList(0,Math.min(6,c.friends.size()))) {
            drawHint(x,yy,"•  "+name);
            addDrawableChild(ButtonWidget.builder(Text.literal("Удалить"),b->{c.friends.remove(name);c.save();init();}).dimensions(x+w-78,yy-4,78,22).build()); yy+=29;
        }
        if(c.friends.isEmpty()) drawHint(x,y+82,"Пока никого нет — добавь друга по нику.");
    }
    private void toggle(int x,int y,int w,String label,boolean value,java.util.function.Consumer<Boolean> setter) {
        addDrawableChild(ButtonWidget.builder(Text.literal((value?"§a●  ":"§7○  ")+label),b->{setter.accept(!value);c.save();init();}).dimensions(x,y,w,26).build());
    }
    private void module(int x,int y,int w,String label,Runnable action) { addDrawableChild(ButtonWidget.builder(Text.literal(label),b->{action.run();}).dimensions(x,y,w,27).build()); }
    private void setTime(long t) { MinecraftClient mc=MinecraftClient.getInstance(); if(mc.world!=null) mc.world.getLevelProperties().setTimeOfDay(t); }
    private void drawHint(int x,int y,String s) { /* rendered in render() from helper labels */ hints.add(new Hint(x,y,s)); }
    private final List<Hint> hints=new ArrayList<>();
    private record Hint(int x,int y,String text) {}
    
    // ВОТ ЗДЕСЬ БЫЛА ОШИБКА. Теперь добавлены mouseX и mouseY в renderBackground
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(panelX,panelY,panelX+panelW,panelY+panelH,0xF0181B25);
        ctx.fill(panelX,panelY,panelX+panelW,panelY+48,0xFF202638);
        ctx.fill(panelX,panelY,panelX+3,panelY+panelH,0xFF42D7E8);
        ctx.drawText(textRenderer,"KRYVEX",panelX+16,panelY+12,0xFF63E6F2,true);
        ctx.drawText(textRenderer,"CLIENT  /  1.21.4",panelX+92,panelY+14,0xFF9AA8BC,false);
        ctx.drawText(textRenderer,"Right Shift — меню",panelX+panelW-132,panelY+17,0xFF9AA8BC,false);
        for (Hint h : hints) ctx.drawText(textRenderer, h.text(), h.x(), h.y(), 0xFFB5C1D3, false);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public boolean shouldPause(){return false;}
}
