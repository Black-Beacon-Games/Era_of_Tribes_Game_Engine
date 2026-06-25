// Perser-Mod Initialisierung
// Fuegt das Volk der Perser mit allen Inhalten zum Spiel hinzu.

var PerserMod = {
    init: function() {
        console.log("Perser-Mod geladen!");
        Engine.tribes.register("perser");
        Engine.technologies.addPersianTechs();
        Engine.buildings.addPersianBuildings();
        Engine.units.addPersianUnits();
        Engine.trigger("onModLoaded", { mod: "neues_volk_mod" });
    }
};
