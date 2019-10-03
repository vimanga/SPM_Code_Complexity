package codecomplexity;
/**/
public class CodeSizeConstrants {
 
	public static final String KEY_WORD_LIST[][]={ 
			{"abstract","assert","boolean",
			"break","byte", "case",	"catch","char","class","const",
			"continue","default","do","double","else","enum",
			"extends","final","finally", "float","for","goto","if",
			"implements","import","instanceof","int","interface",
			"long","native","new","package","private","protected",
			"public","return","short","static","strictfp","super",
			"switch","synchronized","this","throw","throws",
			"transient","try","void","volatile","while","true",
			 "false","null"},
	
			{"break","else","long",
			"switch","case","enum","register","typedef","char","extern",
			"return","union","const","float","short","unsigned","continue",
			"for","signed","void","default","goto","sizeof","volatile","do",
			"if","static","while"},
		
			{"break","else","long",
			"switch","case","enum","register","typedef","char","extern",
			"return","union","const","float","short","unsigned","continue",
			"for","signed","void","default","goto","sizeof","volatile","do",
			"if","static","while","asm","dynamic_cast","namespace",
			"reinterpret_cast","bool","explicit","new","static_cast",
			"catch","false","operator","template","class","friend",
			"private","this","const","cast","inline","public","throw",
			"delete","mutable","protected","true","try","typeid","typename",
			"using","virtual","wchar_t"}};
	
	public static final String[] VARIABLE_START_WITH = {"[A-Za-z_$]","[A-Za-z_]","[A-Za-z_]"};
	
	
	public static final String[] VARIABLE_CHAR = {"[A-Za-z_$0-9]","[A-Za-z_0-9]","[A-Za-z_0-9]"};
	

	
	
	
}
