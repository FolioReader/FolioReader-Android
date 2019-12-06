package com.folioreader.ui.fragment.HighlightNoteFragment;

public class HighlightNotePresenter implements HighlightNoteFrmContract.HighlightNoteFrmPresenter {

    HighlightNoteFrmContract.HighlightNoteView view;
    //private Note model;
   // private Note[] Database = {new Note("Location", "This note has been load from Database", 1)};

    HighlightNotePresenter(HighlightNoteFrmContract.HighlightNoteView view) {
        this.view = view;
        //db = ...getInstance();
    }

    @Override
    public void showNote() {

    //    view.showNoteToView(getNotesFromDB());
    }

    @Override
    public void killDialog() {
        view.killDialogOnView();
    }

    @Override
    public void negativeToVerifyFrm() {
        view.negativeToVerifyFrm();
    }

   // @Override
  //  public void saveNote(Note note, int color_index, String text) {
   //     model = note;
    //    model.setColor(color_index);
    //    model.setText(text);
    //    //Database.save(model);
  //  }

//    @Override
//    public void deleteNote(Note note) {
//        view.deleteNoteOnView(note);
//        view.killDialogOnView();
//    }
//
//    @Override
//    public void deleteNoteInDataBase(Note note) {
//        //Database.deleteNote(note);
//    }
//
//    @Override
//    public Note getNotesFromDB() {
//        return Database[0];
//    }

}
