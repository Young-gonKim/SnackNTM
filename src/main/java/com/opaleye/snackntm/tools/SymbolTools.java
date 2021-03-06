/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opaleye.snackntm.tools;

import java.util.Vector;

public class SymbolTools {

	/**
	 * Returns base from predefined numbers(0~3 -> ATGC)
	 * @author Young-gon Kim, 
	 */
	public static char numberToBase(int number) {
		char ret = 'A';
		switch (number)
		{
		case 0:
			ret = 'A';
			break;
		case 1:
			ret = 'T';
			break;
		case 2: 
			ret = 'G';
			break;
		case 3:
			ret = 'C';
			break;
		}
		return ret;
	}

	/**
	 * Returns corresponding ambiguous symbol from two bases
	 * @author Young-gon Kim, 
	 */
	public static char makeAmbiguousSymbol(char c1, char c2) {
		char ret = 'N';
		c1 = Character.toUpperCase(c1);
		c2 = Character.toUpperCase(c2);
		if(c1 == 'A' && c2 == 'G') ret = 'R';
		else if(c1 == 'G' && c2 == 'A') ret = 'R';

		else if(c1 == 'C' && c2 == 'T') ret = 'Y';
		else if(c1 == 'T' && c2 == 'C') ret = 'Y';

		else if(c1 == 'G' && c2 == 'T') ret = 'K';
		else if(c1 == 'T' && c2 == 'G') ret = 'K';

		else if(c1 == 'C' && c2 == 'A') ret = 'M';
		else if(c1 == 'A' && c2 == 'C') ret = 'M';

		else if(c1 == 'G' && c2 == 'C') ret = 'S';
		else if(c1 == 'C' && c2 == 'G') ret = 'S';

		else if(c1 == 'T' && c2 == 'A') ret = 'W';
		else if(c1 == 'A' && c2 == 'T') ret = 'W';

		if(ret == 'N')
			System.out.println("c1, c2 : " + c1 + ", " + c2);
			
		
		return ret;
	}

	public static Vector<String> IUPACtoSymbolList(char c1) {
		Vector<String> ret = new Vector<String>();
		c1 = Character.toUpperCase(c1);
		
		switch(c1) {
		case 'A': 
			ret.add("A");
			break;
		case 'C':
			ret.add("C");
			break;
		case 'G':
			ret.add("G");
			break;
		case 'T':
			ret.add("T");
			break;
		case 'R':
			ret.add("A");
			ret.add("G");
			break;
		case 'Y':
			ret.add("C");
			ret.add("T");
			break;
		case 'S':
			ret.add("G");
			ret.add("C");
			break;
		case 'W':
			ret.add("A");
			ret.add("T");
			break;
		case 'K':
			ret.add("G");
			ret.add("T");
			break;
		case 'M':
			ret.add("A");
			ret.add("C");
			break;
		case 'B':
			ret.add("C");
			ret.add("G");
			ret.add("T");
			break;
		case 'D':
			ret.add("A");
			ret.add("G");
			ret.add("T");
			break;
		case 'H':
			ret.add("A");
			ret.add("C");
			ret.add("T");
			break;
		case 'V':
			ret.add("A");
			ret.add("C");
			ret.add("G");
			break;
		case 'N':
			ret.add("A");
			ret.add("T");
			ret.add("G");
			ret.add("C");
			break;
		}
		
		
		
		return ret;
	}
	
	
}
