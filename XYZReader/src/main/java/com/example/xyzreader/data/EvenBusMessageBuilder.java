package com.example.xyzreader.data;

import android.database.Cursor;

/**
 * Created by CL on 9/7/17.
 */

public class EvenBusMessageBuilder {
    public static ArticlesDetailMessage BuildDetailMessage(Cursor cursor, int position) {
        ArticlesDetailMessage articlesDetailMessage = new ArticlesDetailMessage();
        articlesDetailMessage.setArticleCursor(cursor);
        articlesDetailMessage.setCurrentPosition(position);
        return articlesDetailMessage;
    }

    public static class ArticlesDetailMessage {
        private Cursor articleCursor;
        private int currentPosition;

        public ArticlesDetailMessage() {
        }

        public Cursor getArticleCursor() {
            return articleCursor;
        }

        public void setArticleCursor(Cursor articleCursor) {
            this.articleCursor = articleCursor;
        }

        public int getCurrentPosition() {
            return currentPosition;
        }

        public void setCurrentPosition(int currentPosition) {
            this.currentPosition = currentPosition;
        }
    }

    public static class DismissProgressDialogMessage {

        private DismissProgressDialogMessage() {
        }

        public static DismissProgressDialogMessage create() {
            return new DismissProgressDialogMessage();
        }
    }
}
