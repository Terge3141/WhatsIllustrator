package helper;

public class SoftBankConverter {

	private static final String[][] MAPPING = { { "e210", "0023-fe0f-20e3" }, { "e225", "0030-fe0f-20e3" },
			{ "e21c", "0031-fe0f-20e3" }, { "e21d", "0032-fe0f-20e3" }, { "e21e", "0033-fe0f-20e3" },
			{ "e21f", "0034-fe0f-20e3" }, { "e220", "0035-fe0f-20e3" }, { "e221", "0036-fe0f-20e3" },
			{ "e222", "0037-fe0f-20e3" }, { "e223", "0038-fe0f-20e3" }, { "e224", "0039-fe0f-20e3" },
			{ "e24e", "00a9-fe0f" }, { "e24f", "00ae-fe0f" }, { "e12d", "1f004" }, { "e532", "1f170-fe0f" },
			{ "e533", "1f171-fe0f" }, { "e535", "1f17e-fe0f" }, { "e14f", "1f17f-fe0f" }, { "e534", "1f18e" },
			{ "e214", "1f192" }, { "e229", "1f194" }, { "e212", "1f195" }, { "e24d", "1f197" }, { "e213", "1f199" },
			{ "e12e", "1f19a" }, { "e513", "1f1e8-1f1f3" }, { "e50e", "1f1e9-1f1ea" }, { "e511", "1f1ea-1f1f8" },
			{ "e50d", "1f1eb-1f1f7" }, { "e510", "1f1ec-1f1e7" }, { "e50f", "1f1ee-1f1f9" }, { "e50b", "1f1ef-1f1f5" },
			{ "e514", "1f1f0-1f1f7" }, { "e512", "1f1f7-1f1fa" }, { "e50c", "1f1fa-1f1f8" }, { "e203", "1f201" },
			{ "e228", "1f202-fe0f" }, { "e216", "1f21a" }, { "e22c", "1f22f" }, { "e22b", "1f233" },
			{ "e22a", "1f235" }, { "e215", "1f236" }, { "e217", "1f237-fe0f" }, { "e218", "1f238" },
			{ "e227", "1f239" }, { "e22d", "1f23a" }, { "e226", "1f250" }, { "e443", "1f300" }, { "e43c", "1f302" },
			{ "e44b", "1f303" }, { "e04d", "1f304" }, { "e449", "1f305" }, { "e146", "1f306" }, { "e44a", "1f307" },
			{ "e44c", "1f308" }, { "e43e", "1f30a" }, { "e04c", "1f319" }, { "e335", "1f31f" }, { "e307", "1f334" },
			{ "e308", "1f335" }, { "e304", "1f337" }, { "e030", "1f338" }, { "e032", "1f339" }, { "e303", "1f33a" },
			{ "e305", "1f33b" }, { "e444", "1f33e" }, { "e110", "1f340" }, { "e118", "1f341" }, { "e119", "1f342" },
			{ "e447", "1f343" }, { "e349", "1f345" }, { "e34a", "1f346" }, { "e348", "1f349" }, { "e346", "1f34a" },
			{ "e345", "1f34e" }, { "e347", "1f353" }, { "e120", "1f354" }, { "e33d", "1f358" }, { "e342", "1f359" },
			{ "e33e", "1f35a" }, { "e341", "1f35b" }, { "e340", "1f35c" }, { "e33f", "1f35d" }, { "e339", "1f35e" },
			{ "e33b", "1f35f" }, { "e33c", "1f361" }, { "e343", "1f362" }, { "e344", "1f363" }, { "e33a", "1f366" },
			{ "e43f", "1f367" }, { "e046", "1f370" }, { "e34c", "1f371" }, { "e34d", "1f372" }, { "e147", "1f373" },
			{ "e043", "1f374" }, { "e338", "1f375" }, { "e30b", "1f376" }, { "e044", "1f378" }, { "e047", "1f37a" },
			{ "e30c", "1f37b" }, { "e314", "1f380" }, { "e112", "1f381" }, { "e34b", "1f382" }, { "e445", "1f383" },
			{ "e033", "1f384" }, { "e448", "1f385" }, { "e117", "1f386" }, { "e440", "1f387" }, { "e310", "1f388" },
			{ "e312", "1f389" }, { "e143", "1f38c" }, { "e436", "1f38d" }, { "e438", "1f38e" }, { "e43b", "1f38f" },
			{ "e442", "1f390" }, { "e446", "1f391" }, { "e43a", "1f392" }, { "e439", "1f393" }, { "e124", "1f3a1" },
			{ "e433", "1f3a2" }, { "e03c", "1f3a4" }, { "e03d", "1f3a5" }, { "e507", "1f3a6" }, { "e30a", "1f3a7" },
			{ "e502", "1f3a8" }, { "e503", "1f3a9" }, { "e125", "1f3ab" }, { "e324", "1f3ac" }, { "e130", "1f3af" },
			{ "e133", "1f3b0" }, { "e42c", "1f3b1" }, { "e03e", "1f3b5" }, { "e326", "1f3b6" }, { "e040", "1f3b7" },
			{ "e041", "1f3b8" }, { "e042", "1f3ba" }, { "e015", "1f3be" }, { "e013", "1f3bf" }, { "e42a", "1f3c0" },
			{ "e132", "1f3c1" }, { "e115", "1f3c3" }, { "e017", "1f3c4" }, { "e131", "1f3c6" }, { "e42b", "1f3c8" },
			{ "e42d", "1f3ca" }, { "e036", "1f3e0" }, { "e038", "1f3e2" }, { "e153", "1f3e3" }, { "e155", "1f3e5" },
			{ "e14d", "1f3e6" }, { "e154", "1f3e7" }, { "e158", "1f3e8" }, { "e501", "1f3e9" }, { "e156", "1f3ea" },
			{ "e157", "1f3eb" }, { "e504", "1f3ec" }, { "e508", "1f3ed" }, { "e505", "1f3ef" }, { "e506", "1f3f0" },
			{ "e52d", "1f40d" }, { "e134", "1f40e" }, { "e529", "1f411" }, { "e528", "1f412" }, { "e52e", "1f414" },
			{ "e52f", "1f417" }, { "e526", "1f418" }, { "e10a", "1f419" }, { "e441", "1f41a" }, { "e525", "1f41b" },
			{ "e019", "1f41f" }, { "e522", "1f420" }, { "e523", "1f424" }, { "e521", "1f426" }, { "e055", "1f427" },
			{ "e527", "1f428" }, { "e530", "1f42b" }, { "e520", "1f42c" }, { "e053", "1f42d" }, { "e52b", "1f42e" },
			{ "e050", "1f42f" }, { "e52c", "1f430" }, { "e04f", "1f431" }, { "e054", "1f433" }, { "e01a", "1f434" },
			{ "e109", "1f435" }, { "e052", "1f436" }, { "e10b", "1f437" }, { "e531", "1f438" }, { "e524", "1f439" },
			{ "e52a", "1f43a" }, { "e051", "1f43b" }, { "e419", "1f440" }, { "e41b", "1f442" }, { "e41a", "1f443" },
			{ "e41c", "1f444" }, { "e22e", "1f446" }, { "e22f", "1f447" }, { "e230", "1f448" }, { "e231", "1f449" },
			{ "e00d", "1f44a" }, { "e41e", "1f44b" }, { "e420", "1f44c" }, { "e00e", "1f44d" }, { "e421", "1f44e" },
			{ "e41f", "1f44f" }, { "e422", "1f450" }, { "e10e", "1f451" }, { "e318", "1f452" }, { "e302", "1f454" },
			{ "e006", "1f455" }, { "e319", "1f457" }, { "e321", "1f458" }, { "e322", "1f459" }, { "e323", "1f45c" },
			{ "e007", "1f45f" }, { "e13e", "1f460" }, { "e31a", "1f461" }, { "e31b", "1f462" }, { "e536", "1f463" },
			{ "e001", "1f466" }, { "e002", "1f467" }, { "e004", "1f468" }, { "e005", "1f469" }, { "e428", "1f46b" },
			{ "e152", "1f46e" }, { "e429", "1f46f" }, { "e515", "1f471" }, { "e516", "1f472" }, { "e517", "1f473" },
			{ "e518", "1f474" }, { "e519", "1f475" }, { "e51a", "1f476" }, { "e51b", "1f477" }, { "e51c", "1f478" },
			{ "e11b", "1f47b" }, { "e04e", "1f47c" }, { "e10c", "1f47d" }, { "e12b", "1f47e" }, { "e11a", "1f47f" },
			{ "e11c", "1f480" }, { "e253", "1f481" }, { "e51e", "1f482" }, { "e51f", "1f483" }, { "e31c", "1f484" },
			{ "e31d", "1f485" }, { "e31e", "1f486" }, { "e31f", "1f487" }, { "e320", "1f488" }, { "e13b", "1f489" },
			{ "e30f", "1f48a" }, { "e003", "1f48b" }, { "e034", "1f48d" }, { "e035", "1f48e" }, { "e111", "1f48f" },
			{ "e306", "1f490" }, { "e425", "1f491" }, { "e43d", "1f492" }, { "e327", "1f493" }, { "e023", "1f494" },
			{ "e328", "1f497" }, { "e329", "1f498" }, { "e32a", "1f499" }, { "e32b", "1f49a" }, { "e32c", "1f49b" },
			{ "e32d", "1f49c" }, { "e437", "1f49d" }, { "e204", "1f49f" }, { "e10f", "1f4a1" }, { "e334", "1f4a2" },
			{ "e311", "1f4a3" }, { "e13c", "1f4a4" }, { "e331", "1f4a6" }, { "e330", "1f4a8" }, { "e05a", "1f4a9" },
			{ "e14c", "1f4aa" }, { "e12f", "1f4b0" }, { "e149", "1f4b1" }, { "e14a", "1f4b9" }, { "e11f", "1f4ba" },
			{ "e00c", "1f4bb" }, { "e11e", "1f4bc" }, { "e316", "1f4bd" }, { "e126", "1f4bf" }, { "e127", "1f4c0" },
			{ "e148", "1f4d6" }, { "e301", "1f4dd" }, { "e00b", "1f4e0" }, { "e14b", "1f4e1" }, { "e142", "1f4e2" },
			{ "e317", "1f4e3" }, { "e103", "1f4e9" }, { "e101", "1f4eb" }, { "e102", "1f4ee" }, { "e00a", "1f4f1" },
			{ "e104", "1f4f2" }, { "e250", "1f4f3" }, { "e251", "1f4f4" }, { "e20b", "1f4f6" }, { "e008", "1f4f7" },
			{ "e12a", "1f4fa" }, { "e128", "1f4fb" }, { "e129", "1f4fc" }, { "e141", "1f50a" }, { "e114", "1f50d" },
			{ "e03f", "1f511" }, { "e144", "1f512" }, { "e145", "1f513" }, { "e325", "1f514" }, { "e24c", "1f51d" },
			{ "e207", "1f51e" }, { "e11d", "1f525" }, { "e116", "1f528" }, { "e113", "1f52b" }, { "e23e", "1f52f" },
			{ "e209", "1f530" }, { "e031", "1f531" }, { "e21a", "1f532" }, { "e21b", "1f533" }, { "e219", "1f534" },
			{ "e024", "1f550" }, { "e025", "1f551" }, { "e026", "1f552" }, { "e027", "1f553" }, { "e028", "1f554" },
			{ "e029", "1f555" }, { "e02a", "1f556" }, { "e02b", "1f557" }, { "e02c", "1f558" }, { "e02d", "1f559" },
			{ "e02e", "1f55a" }, { "e02f", "1f55b" }, { "e03b", "1f5fb" }, { "e509", "1f5fc" }, { "e51d", "1f5fd" },
			{ "e404", "1f601" }, { "e412", "1f602" }, { "e057", "1f603" }, { "e415", "1f604" }, { "e405", "1f609" },
			{ "e056", "1f60a" }, { "e40a", "1f60c" }, { "e106", "1f60d" }, { "e402", "1f60f" }, { "e40e", "1f612" },
			{ "e108", "1f613" }, { "e403", "1f614" }, { "e407", "1f616" }, { "e418", "1f618" }, { "e417", "1f61a" },
			{ "e105", "1f61c" }, { "e409", "1f61d" }, { "e058", "1f61e" }, { "e059", "1f620" }, { "e416", "1f621" },
			{ "e413", "1f622" }, { "e406", "1f623" }, { "e401", "1f625" }, { "e40b", "1f628" }, { "e408", "1f62a" },
			{ "e411", "1f62d" }, { "e40f", "1f630" }, { "e107", "1f631" }, { "e410", "1f632" }, { "e40d", "1f633" },
			{ "e40c", "1f637" }, { "e423", "1f645" }, { "e424", "1f646" }, { "e426", "1f647" }, { "e427", "1f64c" },
			{ "e41d", "1f64f" }, { "e10d", "1f680" }, { "e01e", "1f683" }, { "e435", "1f684" }, { "e01f", "1f685" },
			{ "e434", "1f687" }, { "e039", "1f689" }, { "e159", "1f68c" }, { "e150", "1f68f" }, { "e431", "1f691" },
			{ "e430", "1f692" }, { "e432", "1f693" }, { "e15a", "1f695" }, { "e01b", "1f697" }, { "e42e", "1f699" },
			{ "e42f", "1f69a" }, { "e202", "1f6a2" }, { "e135", "1f6a4" }, { "e14e", "1f6a5" }, { "e137", "1f6a7" },
			{ "e30e", "1f6ac" }, { "e208", "1f6ad" }, { "e136", "1f6b2" }, { "e201", "1f6b6" }, { "e138", "1f6b9" },
			{ "e139", "1f6ba" }, { "e151", "1f6bb" }, { "e13a", "1f6bc" }, { "e140", "1f6bd" }, { "e309", "1f6be" },
			{ "e13f", "1f6c0" }, { "e537", "2122-fe0f" }, { "e237", "2196-fe0f" }, { "e236", "2197-fe0f" },
			{ "e238", "2198-fe0f" }, { "e239", "2199-fe0f" }, { "e23c", "23e9" }, { "e23d", "23ea" },
			{ "e23a", "25b6-fe0f" }, { "e23b", "25c0-fe0f" }, { "e04a", "2600-fe0f" }, { "e049", "2601-fe0f" },
			{ "e009", "260e-fe0f" }, { "e04b", "2614" }, { "e045", "2615" }, { "e00f", "261d-fe0f" },
			{ "e414", "263a-fe0f" }, { "e23f", "2648" }, { "e240", "2649" }, { "e241", "264a" }, { "e242", "264b" },
			{ "e243", "264c" }, { "e244", "264d" }, { "e245", "264e" }, { "e246", "264f" }, { "e247", "2650" },
			{ "e248", "2651" }, { "e249", "2652" }, { "e24a", "2653" }, { "e20e", "2660-fe0f" },
			{ "e20f", "2663-fe0f" }, { "e20c", "2665-fe0f" }, { "e20d", "2666-fe0f" }, { "e123", "2668-fe0f" },
			{ "e20a", "267f" }, { "e252", "26a0-fe0f" }, { "e13d", "26a1" }, { "e018", "26bd" }, { "e016", "26be" },
			{ "e048", "26c4" }, { "e24b", "26ce" }, { "e037", "26ea" }, { "e121", "26f2" }, { "e014", "26f3" },
			{ "e01c", "26f5" }, { "e122", "26fa" }, { "e03a", "26fd" }, { "e313", "2702-fe0f" },
			{ "e01d", "2708-fe0f" }, { "e010", "270a" }, { "e012", "270b" }, { "e011", "270c-fe0f" },
			{ "e32e", "2728" }, { "e206", "2733-fe0f" }, { "e205", "2734-fe0f" }, { "e333", "274c" },
			{ "e020", "2753" }, { "e336", "2754" }, { "e337", "2755" }, { "e021", "2757" }, { "e022", "2764-fe0f" },
			{ "e234", "27a1-fe0f" }, { "e211", "27bf" }, { "e235", "2b05-fe0f" }, { "e232", "2b06-fe0f" },
			{ "e233", "2b07-fe0f" }, { "e32f", "2b50" }, { "e332", "2b55" }, { "e12c", "303d-fe0f" },
			{ "e30d", "3297-fe0f" }, { "e315", "3299-fe0f" }, };

	public static String getNewUnicode(String suggestion) {
		for (String[] x : MAPPING) {
			if (x[0].equals(suggestion)) {
				return x[1];
			}
		}

		return null;
	}

}
