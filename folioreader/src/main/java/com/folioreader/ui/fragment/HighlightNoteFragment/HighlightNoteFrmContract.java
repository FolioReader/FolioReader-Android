package com.folioreader.ui.fragment.HighlightNoteFragment;

public interface HighlightNoteFrmContract {
    interface HighlightNoteView {
        //show note from presenter
        void killDialogOnView();

     //   void showNoteToView(Note note);

        void negativeToVerifyFrm();

     //   void deleteNoteOnView(Note note);
    }

    interface HighlightNoteFrmPresenter {

        void showNote();
        void killDialog();

        void negativeToVerifyFrm();

      //  void saveNote(Note note, int color_index, String text);

     //   void deleteNote(Note note);

     //   void deleteNoteInDataBase(Note note);

     //   Note getNotesFromDB();
    }

}
