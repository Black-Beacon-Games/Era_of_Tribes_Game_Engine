// Era of Tribes - Diplomatie-System

var Diplomacy = {
    treaties: [],
    relations: {},

    init: function() {
        this.treaties = [];
        this.relations = {};
    },

    getRelation: function(tribeA, tribeB) {
        var key = this.getKey(tribeA, tribeB);
        return this.relations[key] || { opinion: 0, trust: 50, treaty: null };
    },

    setRelation: function(tribeA, tribeB, relation) {
        var key = this.getKey(tribeA, tribeB);
        this.relations[key] = relation;
    },

    declareWar: function(from, target) {
        var rel = this.getRelation(from, target);
        rel.treaty = null;
        rel.opinion = -100;
        Engine.trigger("onWarDeclared", { from: from, target: target });
    },

    makePeace: function(tribeA, tribeB) {
        var rel = this.getRelation(tribeA, tribeB);
        rel.treaty = "peace";
        rel.opinion = Math.min(rel.opinion + 30, 50);
        Engine.trigger("onPeaceMade", { tribeA: tribeA, tribeB: tribeB });
    },

    formAlliance: function(tribeA, tribeB) {
        var rel = this.getRelation(tribeA, tribeB);
        rel.treaty = "alliance";
        rel.opinion = Math.min(rel.opinion + 50, 100);
        Engine.trigger("onAllianceFormed", { tribeA: tribeA, tribeB: tribeB });
    },

    tradeAgreement: function(tribeA, tribeB) {
        var rel = this.getRelation(tribeA, tribeB);
        rel.treaty = "trade";
        rel.opinion = Math.min(rel.opinion + 20, 80);
        // Handelsbonus aktivieren
        Engine.trade.addRoute(tribeA, tribeB);
        Engine.trigger("onTradeAgreement", { tribeA: tribeA, tribeB: tribeB });
    },

    getKey: function(a, b) {
        return a < b ? a + "_" + b : b + "_" + a;
    },

    isAtWar: function(tribeA, tribeB) {
        var rel = this.getRelation(tribeA, tribeB);
        return rel.opinion <= -80 || rel.treaty === null;
    },

    isAllied: function(tribeA, tribeB) {
        var rel = this.getRelation(tribeA, tribeB);
        return rel.treaty === "alliance";
    }
};
