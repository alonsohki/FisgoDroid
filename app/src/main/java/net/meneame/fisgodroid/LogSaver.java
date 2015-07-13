package net.meneame.fisgodroid;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

public class LogSaver {
	private Context ctx;
	private List<ChatMessage> msgs;

	public LogSaver(Context context, List<ChatMessage> messages) {
		ctx = context;
		msgs = messages;
	}

	public void save() {
		Toast resultToast = null;
		String logpath = null;

		File log = new File(ctx.getExternalFilesDir(null), DateFormat.format(
				"'fisgona-'dd-MM-yy-kkmmss'.log'", new Date()).toString());

		PrintStream logStream;

		try {
			logpath = log.getCanonicalPath();
			log.createNewFile();

			logStream = new PrintStream(log);

			for (ChatMessage message : msgs) {
				logStream.println(message.logify());
			}

			resultToast = Toast.makeText(ctx,
					ctx.getText(R.string.save_successful), Toast.LENGTH_SHORT);
		} catch (IOException e) {
			resultToast = Toast.makeText(ctx,
					ctx.getText(R.string.save_failed), Toast.LENGTH_SHORT);
			e.printStackTrace();
		} finally {
			resultToast.show();
		}
	}
}
