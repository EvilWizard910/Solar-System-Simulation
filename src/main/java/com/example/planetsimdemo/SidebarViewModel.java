package com.example.planetsimdemo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/*This class will create methods to help go between saved solar systems.*/
public class SidebarViewModel {
    public enum SidebarScreen{
        CONTROLS,SYSTEMS
    }
    public record SystemOption(String id, String label){
        @Override
        public String toString(){
            return label;
        }
    }

    private final ObjectProperty<SidebarScreen> currentScreen=new SimpleObjectProperty<SidebarScreen>(SidebarScreen.CONTROLS);
    private final ObjectProperty<SystemOption> selectedSystem = new SimpleObjectProperty<>();
    private final ObservableList<SystemOption> systems = FXCollections.observableArrayList(
            new SystemOption("__default__","Default Solar System")
    );
    public SidebarViewModel(){
        selectedSystem.set(systems.get(0));
    }

    public void toggleScreen(){
        currentScreen.set(
                currentScreen.get() == SidebarScreen.CONTROLS
                ? SidebarScreen.CONTROLS
                        : SidebarScreen.SYSTEMS
        );
    }

    public ObjectProperty<SidebarScreen> currentScreenProperty(){return currentScreen;}
    public ObjectProperty<SystemOption> selectedSystemProperty(){return selectedSystem;}
    public ObservableList<SystemOption> getSystems(){return systems;}
}
