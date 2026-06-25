// Era of Tribes - Hauptspiel-Logik

var Game = {
    turn: 0,
    maxTurns: 200,
    phase: "start",
    tribes: [],
    currentTribe: 0,
    world: null,

    phases: ["start", "taxes", "production", "research", "trade", "movement", "combat", "events", "end"],

    init: function() {
        this.turn = 0;
        this.phase = "start";
        this.currentTribe = 0;
        this.world = WorldGenerator.generate();
        console.log("Era of Tribes initialisiert.");
    },

    startTurn: function() {
        this.turn++;
        this.phase = "start";
        console.log("=== Runde " + this.turn + " ===");
        this.nextPhase();
    },

    nextPhase: function() {
        var idx = this.phases.indexOf(this.phase);
        if (idx < this.phases.length - 1) {
            this.phase = this.phases[idx + 1];
            Engine.trigger("onPhaseChange", { turn: this.turn, phase: this.phase });
            this.executePhase();
        }
    },

    executePhase: function() {
        switch (this.phase) {
            case "start":    this.doStart();     break;
            case "taxes":    this.doTaxes();     break;
            case "production": this.doProduction(); break;
            case "research": this.doResearch();  break;
            case "trade":    this.doTrade();     break;
            case "movement": this.doMovement();  break;
            case "combat":   this.doCombat();    break;
            case "events":   this.doEvents();    break;
            case "end":      this.doEnd();       break;
        }
    },

    doStart: function() {
        console.log("  Phase: Rundenstart");
        this.nextPhase();
    },

    doTaxes: function() {
        console.log("  Phase: Steuern");
        for (var i = 0; i < this.tribes.length; i++) {
            var tribe = this.tribes[i];
            var taxIncome = Math.floor(tribe.population * 0.5 * tribe.taxRate);
            tribe.gold += taxIncome;
            Engine.trigger("onTaxCollected", { tribe: i, amount: taxIncome });
        }
        this.nextPhase();
    },

    doProduction: function() {
        console.log("  Phase: Produktion");
        for (var i = 0; i < this.tribes.length; i++) {
            var tribe = this.tribes[i];
            for (var j = 0; j < tribe.cities.length; j++) {
                var city = tribe.cities[j];
                for (var k = 0; k < city.buildings.length; k++) {
                    var building = city.buildings[k];
                    Engine.trigger("onBuildingProduces", { tribe: i, city: j, building: building });
                }
            }
        }
        this.nextPhase();
    },

    doResearch: function() {
        console.log("  Phase: Forschung");
        this.nextPhase();
    },

    doTrade: function() {
        console.log("  Phase: Handel");
        this.nextPhase();
    },

    doMovement: function() {
        console.log("  Phase: Bewegung");
        Engine.trigger("onMovementPhase", { turn: this.turn });
    },

    doCombat: function() {
        console.log("  Phase: Kampf");
        Engine.trigger("onCombatPhase", { turn: this.turn });
        this.nextPhase();
    },

    doEvents: function() {
        console.log("  Phase: Ereignisse");
        var activeEvents = EventSystem.getActiveEvents();
        for (var i = 0; i < activeEvents.length; i++) {
            EventSystem.applyEvent(activeEvents[i]);
        }
        this.nextPhase();
    },

    doEnd: function() {
        console.log("  Phase: Rundenende");
        Engine.trigger("onTurnEnd", { turn: this.turn });
        if (this.turn < this.maxTurns) {
            this.startTurn();
        } else {
            Engine.trigger("onGameEnd", { reason: "maxTurnsReached" });
        }
    },

    endTurn: function() {
        if (this.phase !== "movement" && this.phase !== "trade") return;
        this.nextPhase();
    }
};
