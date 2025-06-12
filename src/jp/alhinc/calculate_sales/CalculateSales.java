package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		//コマンドライン引数が渡されているか確認（エラー処理）★
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
		}

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();

		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length; i++) {
			if(files[i].getName().matches("^[0-9]{8}[.]rcd$")) {
				rcdFiles.add(files[i]);  //売上ファイルの条件に当てはまったものをListに追加していく
			}
		}

		//売上ファイルが連番になっているか確認する(エラー処理)★
		for(int i = 0; i < rcdFiles.size() -1; i++) {
			//rcdFiles・・・売上ファイルが入ったリスト
			//rcdFilesに入っているある一つの売上ファイルの名前
			//rcdFileから一つ取り出す　get
			//取り出したファイルの名前　getName

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i+1).getName().substring(0, 8));

			//比較する2つのファイル名の先頭ら数字の8文字を切り出し、int型に変換する
			if((latter - former) != 1) {
				System.out.println("売上ファイル名が連番になっていません");
			}
		}

		//売上ファイルの読込処理
		for(int i = 0; i < rcdFiles.size(); i++) {
			BufferedReader br = null;

			try {
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				//読み込んだものを格納するためのリストを作成
				List<String>fileContents = new ArrayList<>();

				//読み込んだ文字列（一行分）を一旦入る為の変数
				String line;

				//1行ずつ読み込む＝一行分の文字列がStringの変数に一旦入る
				while((line = br.readLine()) != null){

					//保持する
					fileContents.add(line);
				}
				//売上ファイルのフォーマットを確認する(エラー処理)★
				if(fileContents.size() != 2) {
					System.out.println(rcdFiles.get(i).getName() +"のフォーマットが不正です");
				}

				//Mapに特定のkeyが存在するか確認する(エラー処理)★
				if(!branchSales.containsKey(fileContents.get(0))) {
					System.out.println(rcdFiles.get(i).getName() +"の支店コードが不正です");
				}

				/*この段階では、新しいリストの中身は以下のようになっている
				(0)支店コード
				(1)売上金額
				*/

				//売上ファイルから売上金額をMapに加算していくために、型の変換を行う
				long fileSale = Long.parseLong(fileContents.get(1));
				Long saleAmount = branchSales.get(fileContents.get(0)) + fileSale;

				//売上金額が数字なのかを確認する(エラー処理)★
				if(!fileContents.get(1).matches("\\d+")) {
					System.out.println(UNKNOWN_ERROR);
				}

				//合計金額の合計が10桁を超えたかを確認する(エラー処理)★
				if(saleAmount >= 10000000000L) {
					System.out.println("合計金額が10桁を超えました");
				}

				//加算した金額をMapに追加する
				branchSales.put(fileContents.get(0), saleAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);

			//ファイルの存在を確認する(エラー処理)★
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
			}

			//ファイルなのかを確認する（エラー処理）★
			if(file.isFile() && fileName.matches("^[0-9]{8}[.]rcd$")) {
				System.out.println(UNKNOWN_ERROR);
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				//支店定義ファイルのフォーマットを確認する（エラー処理）★
				if((items.length != 2) || (!items[0].matches("[0-9]{3}"))){
					System.out.println(FILE_INVALID_FORMAT);
				}

				//Mapに追加する
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for(String key : branchNames.keySet()) {
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;

		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;

	}

}
