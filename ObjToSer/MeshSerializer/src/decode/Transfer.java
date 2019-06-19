package decode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.threed.jpct.DeSerializer;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;

public class Transfer {
	/**
	 * ��Ҫת���ļ������֣�obj�ļ���mtl���ֱ���һ��
	 */
	private static String[] objList = { "01", "02" };

	public static void main(String[] args) {
		for (int i = 0; i < objList.length; i++) {
			// ����obj��mtl�ļ�·��
			Object3D[] obj3ds = Loader.loadOBJ("d:/obj/undecode/" + objList[i] + ".obj",
					"d:/obj/undecode/" + objList[i] + ".mtl", 1);
			for (Object3D obj : obj3ds) {
				obj.build();
			}

			try {
				// ���ser�ļ���·��
				new DeSerializer().serializeArray(obj3ds, new FileOutputStream("d:/obj/" + objList[i] + ".ser"), true);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
