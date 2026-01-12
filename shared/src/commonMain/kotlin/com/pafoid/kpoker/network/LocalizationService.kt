package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.Language

object LocalizationService {
    private val en = mapOf(
        "app_name" to "KPOKER",
        "username" to "Username",
        "password" to "Password",
        "login" to "Login",
        "register" to "Register",
        "quit" to "Quit Game",
        "remember_me" to "Remember Me",
        "lobby_title" to "Game Lobby",
        "play_vs_house" to "Play vs The House",
        "settings" to "Settings",
        "logout" to "Logout",
        "create_room" to "Create Room",
        "new_room_name" to "New Room Name",
        "no_rooms" to "No active rooms. Create one to start!",
        "players" to "players",
        "in_progress" to "In Progress",
        "waiting" to "Waiting",
        "leave_game" to "Leave Game",
        "pot" to "Pot",
        "start_hand" to "Start Hand",
        "your_hand" to "YOUR HAND",
        "chips" to "CHIPS",
        "current_bet" to "CURRENT BET",
        "fold" to "Fold",
        "check" to "Check",
        "call" to "Call",
        "raise_to" to "Raise to",
        "winner" to "WINNER!",
        "split_pot" to "SPLIT POT!",
        "next_hand_soon" to "Next hand starting soon...",
        "next_hand_in" to "Next hand in",
        "display" to "Display",
        "audio" to "Audio",
        "profile" to "Profile",
        "fullscreen" to "Full Screen Mode",
        "music_vol" to "Music Volume",
        "sfx_vol" to "Sound Effects",
        "new_password" to "New Password",
        "change_password" to "Change Password",
        "new_username" to "New Username",
        "change_username" to "Change Username",
        "back" to "Back",
        "language" to "Language",
        "easy" to "Easy",
        "medium" to "Medium",
        "hard" to "Hard",
        "select_difficulty" to "Select AI Difficulty",
        "rules" to "Rules",
        "poker_rules_title" to "Texas Hold'em Rules",
        "poker_rules_content" to """
            1. Two hole cards are dealt to each player.
            2. Five community cards are dealt face up.
            3. Use your two cards and the five community cards to make the best five-card hand.
            4. Betting rounds: Pre-flop, Flop, Turn, River.
            5. The best hand wins the pot!
        """.trimIndent(),
        "close" to "Close",
        "bet_amount" to "Bet Amount"
    )

    private val fr = mapOf(
        "app_name" to "KPOKER",
        "username" to "Nom d'utilisateur",
        "password" to "Mot de passe",
        "login" to "Connexion",
        "register" to "S'inscrire",
        "quit" to "Quitter",
        "remember_me" to "Se souvenir de moi",
        "lobby_title" to "Salon de jeu",
        "play_vs_house" to "Jouer contre la banque",
        "settings" to "Paramètres",
        "logout" to "Déconnexion",
        "create_room" to "Créer une salle",
        "new_room_name" to "Nom de la salle",
        "no_rooms" to "Aucune salle active. Créez-en une !",
        "players" to "joueurs",
        "in_progress" to "En cours",
        "waiting" to "En attente",
        "leave_game" to "Quitter la partie",
        "pot" to "Pot",
        "start_hand" to "Démarrer la main",
        "your_hand" to "VOTRE MAIN",
        "chips" to "JETONS",
        "current_bet" to "MISE ACTUELLE",
        "fold" to "Se coucher",
        "check" to "Parole",
        "call" to "Suivre",
        "raise_to" to "Relancer à",
        "winner" to "GAGNANT !",
        "split_pot" to "POT PARTAGÉ !",
        "next_hand_soon" to "Prochaine main bientôt...",
        "next_hand_in" to "Prochaine main dans",
        "display" to "Affichage",
        "audio" to "Audio",
        "profile" to "Profil",
        "fullscreen" to "Plein écran",
        "music_vol" to "Volume musique",
        "sfx_vol" to "Effets sonores",
        "new_password" to "Nouveau mot de passe",
        "change_password" to "Changer le mot de passe",
        "new_username" to "Nouveau nom",
        "change_username" to "Changer le nom",
        "back" to "Retour",
        "language" to "Langue",
        "easy" to "Facile",
        "medium" to "Moyen",
        "hard" to "Difficile",
        "select_difficulty" to "Difficulté de l'IA",
        "rules" to "Règles",
        "poker_rules_title" to "Règles du Texas Hold'em",
        "poker_rules_content" to """
            1. Deux cartes fermées sont distribuées à chaque joueur.
            2. Cinq cartes communes sont distribuées face ouverte.
            3. Utilisez vos deux cartes et les cinq cartes communes pour former la meilleure main de cinq cartes.
            4. Tours d'enchères : Pre-flop, Flop, Turn, River.
            5. La meilleure main remporte le pot !
        """.trimIndent(),
        "close" to "Fermer",
        "bet_amount" to "Montant de la mise"
    )

    fun getString(key: String, language: Language): String {
        return (if (language == Language.FRENCH) fr[key] else en[key]) ?: key
    }
}
