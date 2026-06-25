// Era of Tribes - Kampfsystem

var CombatSystem = {
    battles: [],

    resolveBattle: function(attacker, defender, attackerUnits, defenderUnits) {
        var battle = {
            attacker: attacker,
            defender: defender,
            rounds: [],
            winner: null,
            turn: Game.turn
        };

        var atkUnits = attackerUnits.slice();
        var defUnits = defenderUnits.slice();

        var round = 0;
        while (atkUnits.length > 0 && defUnits.length > 0 && round < 20) {
            round++;
            var roundResult = this.resolveRound(atkUnits, defUnits);
            battle.rounds.push(roundResult);
            atkUnits = roundResult.remainingAttackers;
            defUnits = roundResult.remainingDefenders;

            // Moral-Check
            if (this.checkMorale(atkUnits) === "flee") break;
            if (this.checkMorale(defUnits) === "flee") break;
        }

        if (atkUnits.length > 0) {
            battle.winner = attacker;
        } else if (defUnits.length > 0) {
            battle.winner = defender;
        } else {
            battle.winner = "draw";
        }

        this.battles.push(battle);
        Engine.trigger("onBattleResolved", battle);
        return battle;
    },

    resolveRound: function(attackers, defenders) {
        // Angreifer greifen an
        for (var i = 0; i < attackers.length; i++) {
            var target = this.selectTarget(defenders);
            if (!target) break;
            var damage = this.calculateDamage(attackers[i], target);
            target.hp -= damage;
            if (target.hp <= 0) {
                defenders.splice(defenders.indexOf(target), 1);
            }
        }

        // Verteidiger schlagen zurueck
        for (var j = 0; j < defenders.length; j++) {
            var target = this.selectTarget(attackers);
            if (!target) break;
            var damage = this.calculateDamage(defenders[j], target);
            target.hp -= damage;
            if (target.hp <= 0) {
                attackers.splice(attackers.indexOf(target), 1);
            }
        }

        return {
            remainingAttackers: attackers,
            remainingDefenders: defenders,
            attackerLosses: 0,
            defenderLosses: 0
        };
    },

    calculateDamage: function(attacker, defender) {
        var baseDmg = attacker.attack || 1;
        var def = defender.defense || 0;
        var reduction = def / (def + 10);
        var damage = Math.max(1, Math.floor(baseDmg * (1 - reduction) * (0.8 + Math.random() * 0.4)));
        return damage;
    },

    selectTarget: function(units) {
        if (units.length === 0) return null;
        return units[Math.floor(Math.random() * units.length)];
    },

    checkMorale: function(units) {
        var totalMorale = 0;
        for (var i = 0; i < units.length; i++) {
            totalMorale += units[i].morale || 50;
        }
        var avgMorale = totalMorale / units.length;
        if (avgMorale < 15) return "flee";
        return "fight";
    }
};
