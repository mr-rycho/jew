package pl.rychu.jew.gui.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.InputMap;
import javax.swing.KeyStroke;

public class ActionUtil {

	public static void printAllActions(InputMap inputMap) {
		printActions(inputMap, inputMap.allKeys());
	}

	public static void printActions(InputMap inputMap, KeyStroke[] keys) {
		Map<String, KeyStroke> kssm = Arrays.asList(keys).stream()
		 .collect(Collectors.toMap(k->k.toString(), Function.identity()));
		List<String> kss = kssm.keySet().stream().sorted().collect(Collectors.toList());
		for (String ksStr: kss) {
			KeyStroke ks = kssm.get(ksStr);
			System.out.println(ks+" -> "+inputMap.get(ks));
		}
	}

}
