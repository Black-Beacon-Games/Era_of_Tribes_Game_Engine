// Era of Tribes - Ereignis-System

var EventSystem = {
    activeEvents: [],
    eventHistory: [],

    getActiveEvents: function() {
        var triggered = [];
        var events = Engine.assets.getJSON("events/events.json").events;
        for (var i = 0; i < events.length; i++) {
            var evt = events[i];
            if (Math.random() < evt.frequency) {
                triggered.push(evt);
            }
        }
        return triggered;
    },

    applyEvent: function(event) {
        console.log("  Ereignis: " + event.name + " - " + event.description);
        this.activeEvents.push(event);
        this.eventHistory.push({ event: event.id, turn: Game.turn });

        for (var tribeIdx = 0; tribeIdx < Game.tribes.length; tribeIdx++) {
            var tribe = Game.tribes[tribeIdx];
            for (var effect in event.effects) {
                switch (effect) {
                    case "population":
                        tribe.population += event.effects[effect];
                        break;
                    case "gold":
                        tribe.gold += event.effects[effect];
                        break;
                    case "food":
                        tribe.food += event.effects[effect];
                        break;
                    case "happiness":
                        tribe.happiness = Math.max(0, Math.min(100, (tribe.happiness || 50) + event.effects[effect]));
                        break;
                    case "stability":
                        tribe.stability = Math.max(0, Math.min(100, (tribe.stability || 50) + event.effects[effect]));
                        break;
                    case "buildings":
                        for (var c = 0; c < tribe.cities.length; c++) {
                            tribe.cities[c].buildings = Math.max(0, tribe.cities[c].buildings + event.effects[effect]);
                        }
                        break;
                }
            }
        }

        Engine.trigger("onEventApplied", { event: event.id, effects: event.effects });
    }
};
