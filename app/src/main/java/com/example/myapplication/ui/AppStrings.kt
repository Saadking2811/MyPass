package com.example.myapplication.ui

/**
 * Multilingual string system for MyPass — English, French, Arabic
 */
object AppStrings {
    enum class Lang { EN, FR, AR }

    private val strings = mapOf(
        // ── Navigation & General ──
        "app_name" to Triple("MyPass", "MyPass", "MyPass"),
        "welcome" to Triple("Welcome", "Bienvenue", "مرحبا"),
        "welcome_back" to Triple("Welcome Back", "Bon Retour", "مرحبا بعودتك"),
        "dashboard" to Triple("Dashboard", "Tableau de bord", "لوحة التحكم"),
        "profile" to Triple("Profile", "Profil", "الملف الشخصي"),
        "settings" to Triple("Settings", "Paramètres", "الإعدادات"),
        "preferences" to Triple("Preferences", "Préférences", "التفضيلات"),
        "logout" to Triple("Logout", "Déconnexion", "تسجيل الخروج"),
        "save" to Triple("Save", "Enregistrer", "حفظ"),
        "cancel" to Triple("Cancel", "Annuler", "إلغاء"),
        "back" to Triple("Back", "Retour", "رجوع"),
        "continue_text" to Triple("Continue", "Continuer", "متابعة"),
        "done" to Triple("Done", "Terminé", "تم"),
        "loading" to Triple("Loading...", "Chargement...", "جار التحميل..."),
        "error" to Triple("Error", "Erreur", "خطأ"),
        "success" to Triple("Success", "Succès", "نجاح"),
        "offline_mode" to Triple("Offline mode", "Mode hors ligne", "وضع عدم الاتصال"),
        "syncing" to Triple("Syncing...", "Synchronisation...", "جارٍ المزامنة..."),
        "ready" to Triple("Ready", "Prêt", "جاهز"),

        // ── Auth ──
        "sign_in" to Triple("Sign In", "Se connecter", "تسجيل الدخول"),
        "create_account" to Triple("Create Account", "Créer un compte", "إنشاء حساب"),
        "full_name" to Triple("Full Name", "Nom complet", "الاسم الكامل"),
        "email" to Triple("Email", "E-mail", "البريد الإلكتروني"),
        "phone" to Triple("Phone", "Téléphone", "الهاتف"),
        "password" to Triple("Password", "Mot de passe", "كلمة المرور"),
        "already_have_account" to Triple("Already have an account?", "Déjà un compte ?", "لديك حساب بالفعل؟"),
        "google_sign_in" to Triple("Google Sign-In", "Connexion Google", "تسجيل الدخول بجوجل"),
        "digital_checkin" to Triple("Digital Check-in", "Enregistrement numérique", "تسجيل الوصول الرقمي"),

        // ── Dashboard ──
        "new_checkin" to Triple("New Check-In", "Nouvel enregistrement", "تسجيل وصول جديد"),
        "enter_booking_ref" to Triple("Enter booking reference to start", "Entrez la référence de réservation", "أدخل مرجع الحجز للبدء"),
        "start" to Triple("Start", "Démarrer", "ابدأ"),
        "upcoming_flights" to Triple("Upcoming Flights", "Vols à venir", "الرحلات القادمة"),
        "boarding_passes" to Triple("Boarding Passes", "Cartes d'embarquement", "بطاقات الصعود"),
        "my_bookings" to Triple("My Bookings", "Mes réservations", "حجوزاتي"),
        "view_all" to Triple("View All", "Voir tout", "عرض الكل"),

        // ── Flight Lookup ──
        "flight_lookup" to Triple("Flight Lookup", "Recherche de vol", "البحث عن رحلة"),
        "booking_reference" to Triple("Booking Reference", "Référence de réservation", "مرجع الحجز"),
        "last_name" to Triple("Last Name", "Nom de famille", "اسم العائلة"),
        "retrieve" to Triple("Retrieve", "Rechercher", "استرجاع"),
        "sync" to Triple("Sync", "Synchroniser", "مزامنة"),
        "continue_passport" to Triple("Continue to Passport Scan", "Continuer vers le scan du passeport", "متابعة لمسح جواز السفر"),

        // ── Passport ──
        "scan_passport" to Triple("Scan Passport", "Scanner le passeport", "مسح جواز السفر"),
        "passport_verified" to Triple("Passport Verified", "Passeport vérifié", "تم التحقق من جواز السفر"),
        "confirm_continue" to Triple("Confirm & Continue", "Confirmer et continuer", "تأكيد ومتابعة"),
        "position_passport" to Triple("Position your passport in the frame", "Placez votre passeport dans le cadre", "ضع جواز سفرك في الإطار"),
        "camera_permission" to Triple("Camera permission required", "Permission caméra requise", "إذن الكاميرا مطلوب"),
        "grant_permission" to Triple("Grant Permission", "Accorder la permission", "منح الإذن"),
        "passport_number" to Triple("Passport Number", "Numéro de passeport", "رقم جواز السفر"),
        "nationality" to Triple("Nationality", "Nationalité", "الجنسية"),
        "date_of_birth" to Triple("Date of Birth", "Date de naissance", "تاريخ الميلاد"),
        "expiry_date" to Triple("Expiry Date", "Date d'expiration", "تاريخ الانتهاء"),
        "gender" to Triple("Gender", "Sexe", "الجنس"),

        // ── Details Review ──
        "review_details" to Triple("Review Details", "Vérifier les détails", "مراجعة التفاصيل"),
        "flight_info" to Triple("Flight Information", "Informations de vol", "معلومات الرحلة"),
        "passenger" to Triple("Passenger", "Passager", "الراكب"),
        "flight" to Triple("Flight", "Vol", "الرحلة"),
        "route" to Triple("Route", "Itinéraire", "المسار"),
        "departure" to Triple("Departure", "Départ", "المغادرة"),
        "arrival" to Triple("Arrival", "Arrivée", "الوصول"),
        "aircraft" to Triple("Aircraft", "Avion", "الطائرة"),
        "continue_seat" to Triple("Continue to Seat Selection", "Continuer vers la sélection du siège", "متابعة لاختيار المقعد"),

        // ── Seat Selection ──
        "select_seat" to Triple("Select Your Seat", "Choisissez votre siège", "اختر مقعدك"),
        "cabin_layout" to Triple("Aircraft cabin layout", "Plan de la cabine", "مخطط المقصورة"),
        "available" to Triple("Available", "Disponible", "متاح"),
        "premium" to Triple("Premium", "Premium", "مميز"),
        "selected" to Triple("Selected", "Sélectionné", "محدد"),
        "taken" to Triple("Taken", "Occupé", "مشغول"),
        "selected_seat" to Triple("Selected Seat", "Siège sélectionné", "المقعد المحدد"),
        "confirm_seat" to Triple("Confirm Seat", "Confirmer le siège", "تأكيد المقعد"),

        // ── Baggage ──
        "baggage" to Triple("Baggage", "Bagages", "الأمتعة"),
        "declare_luggage" to Triple("Declare your luggage", "Déclarez vos bagages", "أعلن عن أمتعتك"),
        "checked_bags" to Triple("Checked Bags (23kg each)", "Bagages en soute (23kg)", "حقائب مسجلة (23 كجم)"),
        "carry_on" to Triple("Carry-on Bags (7kg each)", "Bagages cabine (7kg)", "حقائب يد (7 كجم)"),
        "oversized" to Triple("Oversized Items", "Articles surdimensionnés", "أغراض كبيرة الحجم"),
        "save_continue" to Triple("Save & Continue", "Enregistrer et continuer", "حفظ ومتابعة"),

        // ── Special Requests ──
        "special_requests" to Triple("Special Requests", "Demandes spéciales", "طلبات خاصة"),
        "additional_needs" to Triple("Any additional needs for your flight?", "Des besoins supplémentaires ?", "أي احتياجات إضافية لرحلتك؟"),
        "dietary_pref" to Triple("Dietary Preference", "Préférence alimentaire", "تفضيل غذائي"),
        "wheelchair" to Triple("Need wheelchair/assistance", "Besoin de fauteuil roulant/assistance", "بحاجة إلى كرسي متحرك/مساعدة"),
        "traveling_infant" to Triple("Traveling with infant", "Voyage avec bébé", "السفر مع رضيع"),
        "traveling_pet" to Triple("Traveling with pet", "Voyage avec animal", "السفر مع حيوان أليف"),
        "additional_notes" to Triple("Additional Notes", "Notes supplémentaires", "ملاحظات إضافية"),
        "complete_checkin" to Triple("Complete Check-In", "Terminer l'enregistrement", "إتمام تسجيل الوصول"),

        // ── Boarding Pass ──
        "checkin_complete" to Triple("Check-In Complete!", "Enregistrement terminé !", "اكتمل تسجيل الوصول!"),
        "boarding_pass_ready" to Triple("Your boarding pass is ready", "Votre carte d'embarquement est prête", "بطاقة الصعود جاهزة"),
        "boarding_pass" to Triple("BOARDING PASS", "CARTE D'EMBARQUEMENT", "بطاقة الصعود"),
        "seat" to Triple("SEAT", "SIÈGE", "المقعد"),
        "gate" to Triple("GATE", "PORTE", "البوابة"),
        "boarding" to Triple("BOARDING", "EMBARQUEMENT", "الصعود"),
        "terminal" to Triple("TERMINAL", "TERMINAL", "المحطة"),
        "class_label" to Triple("CLASS", "CLASSE", "الدرجة"),
        "sequence" to Triple("SEQUENCE", "SÉQUENCE", "التسلسل"),
        "save_pdf" to Triple("Save PDF", "Enregistrer PDF", "حفظ PDF"),
        "new_checkin_btn" to Triple("New Check-In", "Nouvel enregistrement", "تسجيل وصول جديد"),
        "scan_at_gate" to Triple("Scan at gate for boarding", "Scannez à la porte d'embarquement", "امسح عند البوابة للصعود"),
        "previous_passes" to Triple("Previous Passes", "Cartes précédentes", "البطاقات السابقة"),

        // ── Profile ──
        "my_profile" to Triple("My Profile", "Mon profil", "ملفي الشخصي"),
        "personal_info" to Triple("Personal Information", "Informations personnelles", "المعلومات الشخصية"),
        "passport_info" to Triple("Passport Information", "Informations du passeport", "معلومات جواز السفر"),
        "edit_profile" to Triple("Edit Profile", "Modifier le profil", "تعديل الملف الشخصي"),
        "saved_successfully" to Triple("Saved successfully", "Enregistré avec succès", "تم الحفظ بنجاح"),
        "member_since" to Triple("Member since", "Membre depuis", "عضو منذ"),
        "total_flights" to Triple("Total Flights", "Total des vols", "إجمالي الرحلات"),
        "checkins_done" to Triple("Check-ins Done", "Enregistrements effectués", "عمليات تسجيل الوصول"),

        // ── Preferences / Settings ──
        "appearance" to Triple("Appearance", "Apparence", "المظهر"),
        "dark_mode" to Triple("Dark Mode", "Mode sombre", "الوضع الداكن"),
        "dark_mode_desc" to Triple("Switch to dark theme", "Passer au thème sombre", "التبديل إلى المظهر الداكن"),
        "language_label" to Triple("Language", "Langue", "اللغة"),
        "english" to Triple("English", "Anglais", "الإنجليزية"),
        "french" to Triple("French", "Français", "الفرنسية"),
        "arabic" to Triple("Arabic", "Arabe", "العربية"),
        "notifications" to Triple("Notifications", "Notifications", "الإشعارات"),
        "push_notifications" to Triple("Push Notifications", "Notifications push", "إشعارات الدفع"),
        "push_desc" to Triple("Receive flight updates", "Recevoir les mises à jour de vol", "استقبال تحديثات الرحلات"),
        "security" to Triple("Security", "Sécurité", "الأمان"),
        "biometric" to Triple("Biometric Login", "Connexion biométrique", "تسجيل الدخول البيومتري"),
        "biometric_desc" to Triple("Use fingerprint or face", "Utiliser l'empreinte ou le visage", "استخدام البصمة أو الوجه"),
        "about" to Triple("About", "À propos", "حول"),
        "version" to Triple("Version", "Version", "الإصدار"),
        "app_version" to Triple("MyPass v1.0.0", "MyPass v1.0.0", "MyPass v1.0.0"),
        "rate_app" to Triple("Rate this App", "Noter cette application", "قيّم التطبيق"),
        "help_support" to Triple("Help & Support", "Aide et support", "المساعدة والدعم"),
        "privacy_policy" to Triple("Privacy Policy", "Politique de confidentialité", "سياسة الخصوصية"),

        // ── Booking History ──
        "booking_history" to Triple("Booking History", "Historique des réservations", "سجل الحجوزات"),
        "current_bookings" to Triple("Current Bookings", "Réservations actuelles", "الحجوزات الحالية"),
        "past_bookings" to Triple("Past Bookings", "Réservations passées", "الحجوزات السابقة"),
        "no_bookings" to Triple("No bookings yet", "Aucune réservation", "لا توجد حجوزات بعد"),
        "checked_in" to Triple("Checked-In", "Enregistré", "مسجل"),
        "not_checked_in" to Triple("Not Checked-In", "Non enregistré", "غير مسجل"),
        "view_pass" to Triple("View Pass", "Voir la carte", "عرض البطاقة"),

        // ── Additional keys ──
        "save_profile" to Triple("Save Profile", "Enregistrer le profil", "حفظ الملف الشخصي"),
        "saved" to Triple("Saved", "Enregistré", "تم الحفظ"),
        "biometric_auth" to Triple("Biometric Authentication", "Authentification biométrique", "المصادقة البيومترية"),
        "notifications_security" to Triple("Notifications & Security", "Notifications et sécurité", "الإشعارات والأمان"),
        "copyright" to Triple("© 2025 MyPass — Air Algérie Check-In", "© 2025 MyPass — Enregistrement Air Algérie", "© 2025 MyPass — تسجيل وصول الخطوط الجوية الجزائرية"),
        "no_flights" to Triple("No flights found", "Aucun vol trouvé", "لم يتم العثور على رحلات"),
        "no_boarding_passes" to Triple("No boarding passes yet", "Aucune carte d'embarquement", "لا توجد بطاقات صعود بعد"),
        "flights" to Triple("Flights", "Vols", "الرحلات"),
        "back" to Triple("Back", "Retour", "رجوع"),
    )

    fun get(key: String, lang: Lang): String {
        val triple = strings[key] ?: return key
        return when (lang) {
            Lang.EN -> triple.first
            Lang.FR -> triple.second
            Lang.AR -> triple.third
        }
    }
}
