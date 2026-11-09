-- Sample data. Run after schema.sql:  mysql -ustudent -p < database/seed.sql
USE powerconnect;

-- All users have the password: password123
-- (the long text is its BCrypt hash, made with BCryptPasswordEncoder)
INSERT INTO users (user_code, role, full_name, email, phone, area, service_type, language, password_hash) VALUES
('ADM-001', 'ADMIN',      'Chanda Mulenga',    'admin@powerconnect.zm',  '0977000001', NULL,        NULL,          'en',  '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('TEC-001', 'TECHNICIAN', 'Mwila Banda',       'mwila@powerconnect.zm',  '0977000002', 'Matero',    'ELECTRICITY', 'bem', '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('TEC-002', 'TECHNICIAN', 'Grace Tembo',       'grace@powerconnect.zm',  '0977000003', 'Kabulonga', 'ELECTRICITY', 'en',  '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('TEC-003', 'TECHNICIAN', 'Joseph Phiri',      'joseph@powerconnect.zm', '0977000004', 'Chilenje',  'WATER',       'ny',  '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('TEC-004', 'TECHNICIAN', 'Mutale Chishimba',  'mutale@powerconnect.zm', '0977000005', 'Kabwata',   'SANITATION',  'bem', '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('CM-001',  'COMMUNITY',  'Natasha Zulu',      'natasha@example.com',    '0966000001', 'Kabulonga', NULL,          'en',  '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('CM-002',  'COMMUNITY',  'Bwalya Mwansa',     'bwalya@example.com',     '0966000002', 'Matero',    NULL,          'bem', '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('CM-003',  'COMMUNITY',  'Chipo Nkhata',      'chipo@example.com',      '0966000003', 'Matero',    NULL,          'ny',  '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('CM-004',  'COMMUNITY',  'Lweendo Hamoonga',  'lweendo@example.com',    '0966000004', 'Matero',    NULL,          'en',  '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG'),
('CLR-001', 'COUNCILLOR', 'Cllr. Peter Mwale', 'councillor@powerconnect.zm', '0970000201', 'Matero', NULL,         'en',  '$2a$10$tbxSaHkfrhJR07pfdJML5etYGtFvIyE3YVAtB/yAkimXKHpnKmEWG');

-- Incidents waiting for the admin, so there is something to see on first login.
-- Three electricity incidents in Matero within a few hours: the "smart alerts" on the Reports
-- screen will flag this as a possible transformer fault (reload this script before a demo,
-- because the smart alerts only look at the last 24 hours).
INSERT INTO incidents (reported_by, service_type, category, area, address, description, status, created_at) VALUES
(7, 'ELECTRICITY', 'Power outage', 'Matero',    'Near Matero Market',  'No power on our street since this morning.', 'REPORTED', NOW() - INTERVAL 3 HOUR),
(6, 'WATER',       'No water',     'Kabulonga', 'Lukasu Road',         'Taps have been dry for two days.',           'REPORTED', NOW() - INTERVAL 2 HOUR),
(8, 'ELECTRICITY', 'Street light', 'Matero',    'Matero Main Road',    'All the street lights are off.',             'REPORTED', NOW() - INTERVAL 90 MINUTE),
(9, 'ELECTRICITY', 'Fallen cable', 'Matero',    'Behind the clinic',   'A cable is hanging low after the wind.',     'REPORTED', NOW() - INTERVAL 1 HOUR);

INSERT INTO incident_updates (incident_id, status, note, updated_by, created_at) VALUES
(1, 'REPORTED', 'Incident reported', 7, NOW() - INTERVAL 3 HOUR),
(2, 'REPORTED', 'Incident reported', 6, NOW() - INTERVAL 2 HOUR),
(3, 'REPORTED', 'Incident reported', 8, NOW() - INTERVAL 90 MINUTE),
(4, 'REPORTED', 'Incident reported', 9, NOW() - INTERVAL 1 HOUR);

-- Contacts. SAMPLE DATA ONLY: these names and numbers are made up for the school project.
-- Replace them with the real hotlines and councillors before using the app for real.
INSERT INTO contacts (name, title, service_type, area, phone, email) VALUES
('PowerConnect Helpline',        'General helpline (24 hours)',   NULL,          NULL, '+260 211 000 000', 'help@powerconnect.co.zm'),
('Electricity Emergency Line',   'Electricity faults and fallen cables', 'ELECTRICITY', NULL, '+260 211 000 101', 'power@powerconnect.co.zm'),
('Water and Sewer Line',         'Water and sewer faults',        'WATER',       NULL, '+260 211 000 102', 'water@powerconnect.co.zm'),
('Waste Collection Desk',        'Rubbish and public toilets',    'SANITATION',  NULL, '+260 211 000 103', 'waste@powerconnect.co.zm'),
('Cllr. Peter Mwale',            'Ward councillor', NULL, 'Matero',       '+260 970 000 201', NULL),
('Cllr. Agnes Sakala',           'Ward councillor', NULL, 'Kabulonga',    '+260 970 000 202', NULL),
('Cllr. Moses Daka',             'Ward councillor', NULL, 'Chilenje',     '+260 970 000 203', NULL),
('Cllr. Ruth Ng''andu',          'Ward councillor', NULL, 'Kabwata',      '+260 970 000 204', NULL),
('Cllr. John Mbewe',             'Ward councillor', NULL, 'Chawama',      '+260 970 000 205', NULL),
('Cllr. Esther Lungu',           'Ward councillor', NULL, 'Kanyama',      '+260 970 000 206', NULL),
('Cllr. David Chola',            'Ward councillor', NULL, 'Mtendere',     '+260 970 000 207', NULL),
('Cllr. Mercy Kunda',            'Ward councillor', NULL, 'Woodlands',    '+260 970 000 208', NULL),
('Cllr. Patrick Musonda',        'Ward councillor', NULL, 'Garden',       '+260 970 000 209', NULL),
('Cllr. Faith Mumba',            'Ward councillor', NULL, 'Chelston',     '+260 970 000 210', NULL),
('Cllr. Kelvin Zimba',           'Ward councillor', NULL, 'Kalingalinga', '+260 970 000 211', NULL),
('Cllr. Ireen Bwalya',           'Ward councillor', NULL, 'Northmead',    '+260 970 000 212', NULL);

-- Chat bot answers. The bot checks the rows IN ORDER and uses the first one whose keywords
-- appear in the message, so emergencies come first and general questions come last.
-- NOTE: the Bemba and Nyanja answers are first drafts - ask a native speaker to check them.
INSERT INTO faqs (keywords, answer_en, answer_bem, answer_ny) VALUES

-- 1. Emergencies
('cable,wire,shock,electrocut,spark,fire,smoke,waya,moto',
 'DANGER: stay away from fallen cables, sparks or fire, and keep children away. Do not touch anything. Call the helpline NOW, then report it with "New incident" > Electricity > "Fallen cable".',
 'UBUSANSO: mwiba apepi na ma waya ayawa, ifyasaluka nangu umulilo, kabili mwileka abana ukupalamina. Mwikumya kantu. Tumeni foni ku helpline NOMBA.',
 'NGOZI: musayandikire mawaya agwa, moto kapena zothetheka, ndipo ana asayandikire. Musakhudze kanthu. Imbani helpline TSOPANO.'),

-- 2. Water
('pipe,burst,leak,paipi',
 'For a burst or leaking pipe: close your main water tap if you can, then tap "New incident" > Water > "Burst pipe". Add a landmark so the technician can find it.',
 'Nga ipaipi lyaphuka nangu lilesuma: isaleni umupompi ukalamba, elyo tinikeni "New incident" > Water > "Burst pipe". Lembeni icishibilo pa kuti kafundi akasange.',
 'Ngati paipi yaphulika kapena ikudontha: tsekani mpope waukulu ngati mungathe, kenako dinani "New incident" > Water > "Burst pipe". Lembani chizindikiro kuti katswiri apeze malo.'),
('dirty,brown,smell,cholera',
 'Do not drink dirty or smelly water - boil it or use chlorine first. Report it with "New incident" > Water > "Dirty water" so it can be checked.',
 'Mwilanwa amenshi ayakowela nangu ayanunka - ipikeni intanshi nangu bikeni chlorine. Ishibisheni pa "New incident" > Water > "Dirty water".',
 'Musamwe madzi auve kapena onunkha - wiritsani kaye kapena ikani chlorine. Nenani pa "New incident" > Water > "Dirty water".'),
('water,taps,dry,borehole,pressure,madzi,amenshi',
 'For water problems (no water, low pressure), tap "New incident" > Water and choose the problem. You will get an alert at every step of the repair.',
 'Pa mafya ya menshi, tinikeni "New incident" > Water no kusala ubwafya. Mukalapokelela ubukombe pa cila cintu.',
 'Pa mavuto a madzi, dinani "New incident" > Water ndikusankha vuto. Mudzalandira uthenga pa sitepe iliyonse.'),

-- 3. Sanitation
('sewer,sewage,toilet,drain,flood,chimbudzi,cimbusu',
 'For blocked or overflowing sewers, drains or public toilets, tap "New incident" > Sanitation. Keep children away from sewage and wash your hands with soap.',
 'Pa ma sewer ayaisalika nangu ama toilet, tinikeni "New incident" > Sanitation. Mwileka abana ukupalamina kabili samba ku minwe ne sopo.',
 'Pa ma sewer otsekeka kapena zimbudzi, dinani "New incident" > Sanitation. Ana asayandikire zonyansa ndipo sambani m''manja ndi sopo.'),
('rubbish,garbage,trash,waste,dump,zinyalala',
 'For rubbish that has not been collected, tap "New incident" > Sanitation > "Rubbish not collected". Please do not burn rubbish.',
 'Pa fyakuposa ifishasendwa, tinikeni "New incident" > Sanitation. Mwiocha ifyakuposa.',
 'Pa zinyalala zosatengedwa, dinani "New incident" > Sanitation > "Rubbish not collected". Chonde musawotche zinyalala.'),

-- 4. Electricity
('meter,token,units,prepaid',
 'For meter or token (units) problems, tap "New incident" > Electricity > "Faulty meter". Have your meter number ready.',
 'Pa mafya ya meter nangu ama units, tinikeni "New incident" > Electricity > "Faulty meter". Mube ne nambala ya meter.',
 'Pa mavuto a meter kapena ma units, dinani "New incident" > Electricity > "Faulty meter". Khalani ndi nambala ya meter.'),
('load shedding,loadshedding,schedule',
 'Load shedding is planned, so please check the published schedule for your area first. If your power is off OUTSIDE your load-shedding hours, report it with "New incident" > Electricity > "Power outage".',
 'Load shedding yalipangwa, moneni intanshi inshita ya ncende yenu. Nga amalaiti yaputuka inshita imbi, ishibisheni pa "New incident" > Electricity.',
 'Load shedding imakonzedwa, choncho onani ndandanda ya dera lanu kaye. Ngati magetsi azima kunja kwa nthawi imeneyo, nenani pa "New incident" > Electricity.'),
('power,electric,light,outage,magetsi,malaiti',
 'Sorry about the power problem. Tap "New incident" > Electricity and choose "Power outage" or "Street light". You will get an alert when a technician is on the way.',
 'Twalanguluka pa bwafya bwa malaiti. Tinikeni "New incident" > Electricity. Mukalapokelela ubukombe nga kafundi alelanda.',
 'Pepani chifukwa cha vuto la magetsi. Dinani "New incident" > Electricity ndikusankha "Power outage" kapena "Street light". Mudzalandira uthenga katswiri akakhala pa njira.'),

-- 5. Using the app
('password,forgot,login,log in,sign in',
 'Log in with your email and password. New here? Tap "Create an account". If you forgot your password, call the helpline or email us and we will help you get back in.',
 'Ingileni ne email ne shiwi lya nkama. Muli abapya? Tinikeni "Create account". Nga mwalabile ishiwi lya nkama, tumeni foni ku helpline nangu email.',
 'Lowani ndi imelo ndi chinsinsi. Ndinu watsopano? Dinani "Create an account". Ngati mwaiwala chinsinsi, imbani helpline kapena titumizireni imelo.'),
('register,account,sign up,signup,join',
 'To create an account, tap "New here? Create an account" on the login screen. You need your name, email, phone number and area. It is free.',
 'Ukupanga account, tinikeni "Create account" pa login. Mufwaya ishina, email, foni ne ncende. Takwaba ukulipila.',
 'Kuti mupange akaunti, dinani "Create account" pa login. Mukufunika dzina, imelo, foni ndi dera lanu. Ndi zaulere.'),
('language,bemba,nyanja,english,chilankhulo,ululimi',
 'You can change the language any time with the English / Bemba / Nyanja buttons at the top of the screen.',
 'Kuti mwalula ululimi inshita iliyonse pa ma button ya English / Bemba / Nyanja pa muulu.',
 'Mutha kusintha chilankhulo nthawi iliyonse ndi mabatani a English / Bemba / Nyanja pamwamba.'),
('not fixed,still,again,broken',
 'If the technician said the work is completed but the problem is still there, do NOT close the incident. Call the helpline and we will send someone again.',
 'Nga kafundi atila napwisha lelo ubwafya bucilipo, mwiisala ilipoti. Tumeni foni ku helpline, tukatuma umbi.',
 'Ngati katswiri wanena kuti watsiriza koma vuto lidakalipo, MUSATSEKE vutolo. Imbani helpline ndipo tidzatumizanso wina.'),
('close,confirm,fixed,done',
 'When the technician marks the work as completed, open the incident and tap "Yes, it is fixed - close". This tells us the problem is really solved.',
 'Nga kafundi napwisha, isukeni ilipoti no kutinika "Yes, it is fixed - close".',
 'Katswiri akatsiriza, tsegulani vutolo ndikudina "Yes, it is fixed - close". Izi zimatiuza kuti vuto latha ndithu.'),
('log,report,incident,complain,lodge,problem,nenani,vuto,ubwafya',
 'To report an incident: tap "New incident", choose the service (Water, Electricity or Sanitation), pick the problem and your area, add a street or landmark, then tap "Send". You will get an alert at every step.',
 'Ukwishibisha ubwafya: tinikeni "New incident", saleni umulimo (Water, Electricity nangu Sanitation), saleni ubwafya ne ncende, lembeni umusebo, elyo tinikeni "Send".',
 'Kuti munene vuto: dinani "New incident", sankhani ntchito (Water, Electricity kapena Sanitation), sankhani vuto ndi dera lanu, lembani msewu kapena chizindikiro, kenako dinani "Send". Mudzalandira uthenga pa sitepe iliyonse.'),
('how long,wait,time,late,when',
 'An admin usually assigns a technician within a few hours. Emergencies (fallen cables, burst mains, overflowing sewers) are handled first.',
 'Kafundi alasalwa mu nshita inono. Ubusanso (ama waya ayawa, amapaipi ayaphuka) bulabombwapo intanshi.',
 'Katswiri amasankhidwa mkati mwa maola ochepa. Zoopsa (mawaya agwa, mapaipi aakulu ophulika, ma sewer osefukira) zimakonzedwa kaye.'),
('status,track,progress,update,alert',
 'Open "My incidents" and tap an incident to see its timeline, with the date and time of every step. New updates also appear in "Alerts".',
 'Isukeni "My incidents" no kutinika pa bwafya ukumona ifyacitika ne nshita. Ubukombe bupya bulamoneka pa "Alerts".',
 'Tsegulani "My incidents" ndikudina vuto kuti muone zomwe zachitika ndi nthawi yake. Zatsopano zimaonekanso mu "Alerts".'),
('pay,cost,charge,free,money,bribe',
 'Reporting an incident is FREE. Never pay a technician cash for a repair - if anyone asks you for money, call the helpline.',
 'Ukwishibisha ubwafya takwaba ukulipila. Mwilalipila kafundi indalama - nga umo alefwaya indalama, tumeni foni ku helpline.',
 'Kunena vuto ndi KWAULERE. Musalipire katswiri ndalama - ngati wina akufunsani ndalama, imbani helpline.'),
('technician,worker,engineer,katswiri,kafundi',
 'The admin picks a technician who works on your service (water, electricity or sanitation). Their name appears in your incident''s timeline once they are assigned.',
 'Admin asala kafundi wa mulimo wenu. Ishina lyakwe likamoneka pa fyacitika nga nasalwa.',
 'Admin amasankha katswiri wa ntchito yanu. Dzina lake limaonekera mu zomwe zachitika akangosankhidwa.'),
('contact,phone,call,email,helpline,number,talk,office',
 'You can call the helpline on +260 211 000 000 or email help@powerconnect.co.zm. For emergencies like fallen cables, always call.',
 'Kuti mwatumina foni ku +260 211 000 000 nangu email ku help@powerconnect.co.zm.',
 'Mutha kuimba helpline pa +260 211 000 000 kapena imelo ku help@powerconnect.co.zm. Pa zoopsa ngati mawaya agwa, imbani nthawi zonse.'),

-- 6. Small talk
('thank,zikomo,natotela',
 'You are welcome! Is there anything else I can help with?',
 'Mwatotela! Bushe kuli fimbi ifyo nkamwafwa?',
 'Zikomo kwambiri! Pali china chomwe ndingakuthandizeni?'),
('hello,hey,good morning,good afternoon,moni,mwapoleni,muli bwanji',
 'Hello! I can help you report water, electricity or sanitation problems, follow your incident, or find our contact details. What do you need?',
 'Mwapoleni! Kuti namwafwa ukwishibisha amafya ya menshi, malaiti nangu ubusaka. Cinshi mulefwaya?',
 'Moni! Ndingakuthandizeni kunena mavuto a madzi, magetsi kapena ukhondo, kutsatira vuto lanu, kapena kupeza manambala athu. Mukufuna chiyani?'),

-- Used when nothing matches
('default',
 'Sorry, I don''t know that one yet. Try words like report, water, power, sewer, meter or status - or call the helpline on +260 211 000 000.',
 'Mutulekelele, tatulishiba ici. Eseni amashiwi nga report, amenshi, malaiti, sewer nangu meter - nangu tumeni foni ku helpline.',
 'Pepani, sitikudziwa izi. Yesani mawu ngati report, madzi, magetsi, sewer kapena meter - kapena imbani helpline pa +260 211 000 000.');
