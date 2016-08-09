/*
* Copyright (C) 2016 Pedro Paulo de Amorim
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.folioreader.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.adapter.FolioPageFragmentAdapter;
import com.folioreader.adapter.TOCAdapter;
import com.folioreader.database.HighlightTable;
import com.folioreader.fragments.FolioPageFragment;
import com.folioreader.model.Highlight;
import com.folioreader.smil.AudioElement;
import com.folioreader.smil.SequenceElement;
import com.folioreader.smil.SmilElement;
import com.folioreader.smil.SmilFile;
import com.folioreader.smil.SmilParser;
import com.folioreader.smil.TextElement;
import com.folioreader.util.AppUtil;
import com.folioreader.util.EpubManipulator;
import com.folioreader.view.AudioView;
import com.folioreader.view.AudioViewCallback;
import com.folioreader.view.ConfigView;
import com.folioreader.view.ConfigViewCallback;
import com.folioreader.view.FolioView;
import com.folioreader.view.FolioViewCallback;
import com.folioreader.view.VerticalViewPager;

import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class FolioActivity extends AppCompatActivity implements ConfigViewCallback,
        FolioViewCallback, FolioPageFragment.FolioPageFragmentCallback, TOCAdapter.ChapterSelectionCallBack{

    public static final String INTENT_EPUB_ASSET_PATH = "com.folioreader.epub_asset_path";
    public static final int ACTION_HIGHLIGHT_lIST = 77;
    private static final String HIGHLIGHT_ITEM ="highlight_item" ;
    private static final String ITEM_DELETED = "item_deleted";

    private RecyclerView recyclerViewMenu;
    private VerticalViewPager mFolioPageViewPager;
    private FolioView folioView;
    private ConfigView configView;
    private AudioView audioView;
    private Toolbar mToolbar;
    List<AudioElement> mAudioElementArrayList;

    private String mEpubAssetPath;
    private int mAudioPagePosition;
    private Book mBook;
    private ArrayList<TOCReference> mTocReferences = new ArrayList<>();
    private List<SpineReference> mSpineReferences;
    private List<String> mSpineReferenceHtmls = new ArrayList<>();
    private boolean mIsActionBarVisible = false;
    private TOCAdapter mTocAdapter;
    private int mChapterPosition;
    private ActionMode mActionMode = null;

    private boolean mHighLight = false;
    List<TextElement> mTextElementList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folio_activity);
        mEpubAssetPath = getIntent().getStringExtra(INTENT_EPUB_ASSET_PATH);
        loadBook();
       // AppUtil.copyAssets(FolioActivity.this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        findViewById(R.id.btn_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.drawer_menu);
                if (!((DrawerLayout) findViewById(R.id.drawer_left)).isDrawerOpen(relativeLayout)) {
                    ((DrawerLayout) findViewById(R.id.drawer_left)).openDrawer(relativeLayout);
                } else {
                    ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawer(relativeLayout);
                }
            }
        });

        findViewById(R.id.btn_speaker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(audioView.getVisibility()==View.GONE){
                    audioView.setVisibility(View.VISIBLE);
                }*/
                if (audioView.isDragViewAboveTheLimit()) {
                    audioView.moveToOriginalPosition();
                } else {
                    audioView.moveOffScreen();
                }
            }
        });
    }

    private void loadBook() {
        AssetManager assetManager = getAssets();
        try {
            final InputStream epubInputStream = assetManager.open(mEpubAssetPath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AppUtil.saveEpubFile(epubInputStream,FolioActivity.this);
                }
            }).start();

            /*mBook = (new EpubReader()).readEpub(epubInputStream);


            populateTableOfContents(mBook.getTableOfContents().getTocReferences(), 0);
            Spine spine = new Spine(mBook.getTableOfContents());
            this.mSpineReferences = spine.getSpineReferences();
            for (int i = 0; i < mSpineReferences.size(); i++) mSpineReferenceHtmls.add(null);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // DO your work here
                    parseSmil();
                    Log.e("smilfile","smilfile parsed");
                }

            }).start();
*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateTableOfContents(List<TOCReference> tocReferences, int depth) {
        if (tocReferences == null) {
            return;
        }

        for (TOCReference tocReference : tocReferences) {
            mTocReferences.add(tocReference);
            populateTableOfContents(tocReference.getChildren(), depth + 1);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        recyclerViewMenu = (RecyclerView) findViewById(R.id.recycler_view_menu);
        folioView = (FolioView) findViewById(R.id.folio_view);
        configView = (ConfigView) findViewById(R.id.config_view);
        audioView=(AudioView) findViewById(R.id.audio_view);

       /* audioView.postDelayed(new Runnable() {
            @Override
            public void run() {
                audioView.moveOffScreen();
                configView.moveOffScreen();
            }
        },0);
*/
        //configRecyclerViews();
        //configFolio();
        configDrawerLayoutButtons();
    }

    @Override
    public void onBackPressed() {
        if (configView.isDragViewAboveTheLimit()) {
            configView.moveToOriginalPosition();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackgroundUpdate(int value) {
        recyclerViewMenu.setBackgroundColor(value);
        folioView.setBackgroundColor(value);

    }

    @Override
    public void changeMenuTextColor() {
        mTocAdapter.setNightMode(!Config.getConfig().isNightMode());
        mTocAdapter.setSelectedChapterPosition(mChapterPosition);
        mTocAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShadowAlpha(float alpha) {
        folioView.updateShadowAlpha(alpha);
    }

    @Override
    public void showShadow() {
        folioView.resetView();
    }

    @Override
    public void onConfigChange() {

        int position = mFolioPageViewPager.getCurrentItem();
        //reload previous, current and next fragment
        Fragment page;
        if (position != 0) {
            page = getFragment(position - 1);
            ((FolioPageFragment) page).reload();
        }
        page = getFragment(position);
        ((FolioPageFragment) page).reload();
        if (position < mSpineReferences.size()) {
            page = getFragment(position + 1);
            ((FolioPageFragment) page).reload();
        }
    }

    @Override
    public void onAudioPlayed() {
        //mFolioPageViewPager.setCurrentItem(mAudioPagePosition);
    }

    private Fragment getFragment(int pos) {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.folioPageViewPager + ":" + (pos));
    }

    @Override
    public void onShadowClick() {
        configView.moveOffScreen();
        audioView.moveOffScreen();
    }

    public void configRecyclerViews(ArrayList<TOCReference> tocReferences,Book book,ArrayList<SpineReference> spineReferenceArrayList) {
        mTocReferences=tocReferences;
        mSpineReferences=spineReferenceArrayList;
        mBook=book;
        configFolio();
        parseSmil();
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        if (mTocReferences != null) {
            mTocAdapter = new TOCAdapter(mTocReferences, FolioActivity.this);
            recyclerViewMenu.setAdapter(mTocAdapter);
        }
    }

    /*private String InjectInFile(String filePath) {
        *//*String htmlContent = "???";
        if (mPosition != -1) {
            htmlContent = mActivityCallback.getChapterHtmlContent(mPosition);
        }*//*
        String htmlContent=EpubManipulator.readPage(filePath);

        String cssPath = String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">", "file:///android_asset/Style.css");
        String jsPath = String.format("<script type=\"text/javascript\" src=\"%s\"></script>", "file:///android_asset/Bridge.js");
        jsPath = jsPath + String.format("<script type=\"text/javascript\" src=\"%s\"></script>", "file:///android_asset/jquery-1.8.3.js");
        jsPath = jsPath + String.format("<script type=\"text/javascript\" src=\"%s\"></script>", "file:///android_asset/jpntext.js");
        jsPath = jsPath + String.format("<script type=\"text/javascript\" src=\"%s\"></script>", "file:///android_asset/rangy-core.js");
        jsPath = jsPath + String.format("<script type=\"text/javascript\" src=\"%s\"></script>", "file:///android_asset/rangy-serializer.js");
        jsPath = jsPath + String.format("<script type=\"text/javascript\" src=\"%s\"></script>", "file:///android_asset/android.selection.js");
        String toInject = "\n" + cssPath + "\n" + jsPath + "\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "";
        Config config = Config.getConfig();
        switch (config.getFont()) {
            case 0:
                classes = "andada";
                break;
            case 1:
                classes = "lato";
                break;
            case 2:
                classes = "lora";
                break;
            case 3:
                classes = "raleway";
                break;
            default:
                break;
        }

        if (config.isNightMode()) {
            classes += " nightMode";
        }

        switch (config.getFontSize()) {
            case 0:
                classes += " textSizeOne";
                break;
            case 1:
                classes += " textSizeTwo";
                break;
            case 2:
                classes += " textSizeThree";
                break;
            case 3:
                classes += " textSizeFour";
                break;
            case 4:
                classes += " textSizeFive";
                break;
            default:
                break;
        }

        htmlContent = htmlContent.replace("<html ", "<html class=\"" + classes + "\" ");
        ArrayList<Highlight> highlights = (ArrayList<Highlight>) HighlightTable.getAllHighlight(getActivity().getApplication(), mBook.getTitle(), mPosition);
        for (Highlight highlight : highlights) {
            String highlightStr = "<highlight id=\"" + highlight.getHighlightId() + "\" onclick=\"callHighlightURL(this);\" class=\"" + highlight.getType() + "\">" + highlight.getContent() + "</highlight>";
            String searchStr = highlight.getContentPre() + "" + highlight.getContent() + "" + highlight.getContentPost();
            htmlContent = htmlContent.replace(searchStr, highlightStr);
        }
        return htmlContent;
    }*/




    private void configFolio() {
        mFolioPageViewPager = (VerticalViewPager) folioView.findViewById(R.id.folioPageViewPager);
        mFolioPageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mChapterPosition = position;
                mTocAdapter.setNightMode(Config.getConfig().isNightMode());
                mTocAdapter.setSelectedChapterPosition(mChapterPosition);
                mTocAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mBook != null && mSpineReferences != null) {
            FolioPageFragmentAdapter folioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(), mSpineReferences, mBook);
            mFolioPageViewPager.setAdapter(folioPageFragmentAdapter);
        }

        folioView.setFolioViewCallback(FolioActivity.this);
        configView.setConfigViewCallback(FolioActivity.this);
        audioView.setAudioViewCallback(FolioActivity.this);
    }

    private void configDrawerLayoutButtons() {
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawers();
                finish();
            }
        });
        findViewById(R.id.btn_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawers();
                if (configView.isDragViewAboveTheLimit()) {
                    configView.moveToOriginalPosition();
                } else {
                    configView.moveOffScreen();
                }
            }
        });

        findViewById(R.id.btn_highlight_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawer((RelativeLayout) findViewById(R.id.drawer_menu));
                Intent intent = new Intent(FolioActivity.this, HighlightListActivity.class);
                startActivityForResult(intent,ACTION_HIGHLIGHT_lIST);
            }
        });
    }

    @Override
    public String getChapterHtmlContent(int position) {
        return readHTmlString(position);
        //return readHTmlString(position);
    }

    @Override
    public void hideOrshowToolBar() {
        Log.d("in hideOrshowToolBar","main");
        if (mIsActionBarVisible)
            toolbarAnimateHide();
        else {
            toolbarAnimateShow(1);
        }
    }

    @Override
    public void hideToolBarIfVisible() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide();
        }
    }

    @Override
    public void invalidateActionMode() {
    }


    private String readHTmlString(int position) {
        String pageHref=mSpineReferences.get(position).getResource().getHref();
        pageHref=Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/"+pageHref;
        String html = EpubManipulator.readPage(pageHref);
        //String replacement="src=\""+"file:///storage/emulated/0/folioreader/temp/OEBPS/Images/cover.png\" width=45 height=45";
        /*String replacement="src=\""+"file://"+Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/";
       // html.replaceAll("../",");
        Pattern p=Pattern.compile("src=\"../");
        Matcher m=p.matcher(html);
        StringBuffer stringBuffer=new StringBuffer();
        while(m.find()){
            m.appendReplacement(stringBuffer,replacement);
        }
        m.appendTail(stringBuffer);
        Log.d("string",stringBuffer.toString());*/
        /*String html= null;
        try {
            html = AppUtil.readerSmil(mSpineReferences.get(position).getResource().getReader());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return html;
        /*String pageHref=mSpineReferences.get(position).getResource().getHref();
        pageHref="file:///"+Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/"+pageHref;*/
        //return pageHref;
    }
    private String reader(int position) {
        if (mSpineReferenceHtmls.get(position) != null) {
            return mSpineReferenceHtmls.get(position);
        } else {
            try {
                Reader reader = mSpineReferences.get(position).getResource().getReader();

                StringBuilder builder = new StringBuilder();
                int numChars;
                char[] cbuf = new char[2048];
                while ((numChars = reader.read(cbuf)) >= 0) {
                    builder.append(cbuf, 0, numChars);
                }
                String content = builder.toString();
                mSpineReferenceHtmls.set(position, content);
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }


   /* public String replace(CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(this).replaceAll(Matcher.quoteReplacement(replacement.toString()));
    }*/


    private void toolbarAnimateShow(final int verticalOffset) {
        mToolbar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        toolbarSetElevation(verticalOffset == 0 ? 0 : 1);
                    }
                });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsActionBarVisible) {
                            toolbarAnimateHide();
                        }
                    }
                });
            }
        }, 10000);

        mIsActionBarVisible = true;
    }

    private void toolbarAnimateHide() {
        mToolbar.animate()
                .translationY(-mToolbar.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toolbarSetElevation(0);
                    }
                });
        mIsActionBarVisible = false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void toolbarSetElevation(float elevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(elevation);
        }
    }

    @Override
    public void onChapterSelect(int position) {
        mFolioPageViewPager.setCurrentItem(position);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.drawer_menu);
        ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawer(relativeLayout);
    }

    private String getSelectedTextFromPage() {
        int currentPosition = mFolioPageViewPager.getCurrentItem();
        FolioPageFragment folioPageFragment = (FolioPageFragment) getFragment(currentPosition);
        String selectedText = folioPageFragment.getSelectedText();
        return selectedText;
    }

    public Highlight setCurrentPagerPostion(Highlight highlight){
        highlight.setCurrentPagerPostion(mFolioPageViewPager.getCurrentItem());
        return  highlight;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==ACTION_HIGHLIGHT_lIST && resultCode==RESULT_OK){
            Log.d("*****","crash in folioActivity");
            if(data.hasExtra(HIGHLIGHT_ITEM)) {
                Highlight highlight= data.getParcelableExtra(HIGHLIGHT_ITEM);
                int position=highlight.getCurrentPagerPostion();
                mFolioPageViewPager.setCurrentItem(position);
                Fragment fragment=getFragment(position);
                ((FolioPageFragment) fragment).setWebViewPosition(highlight.getCurrentWebviewScrollPos());
            } else if(data.hasExtra(ITEM_DELETED)){
                ((FolioPageFragment)getFragment(mChapterPosition)).reload();

            }
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }

    /*@Override
    public void onActionModeStarted(ActionMode mode) {
        String selectedText = getSelectedTextFromPage();
        if (!TextUtils.isEmpty(selectedText)) {
            if (mActionMode == null) {
                mActionMode = mode;
                Menu menu = mode.getMenu();
                menu.clear();

                // Inflate your own menu items
                mode.getMenuInflater().inflate(R.menu.menu_text_selection, menu);
            }
        }


        super.onActionModeStarted(mode);
    }

    public void onContextualMenuItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.menu_copy) {
            AppUtil.copyToClipboard(this, getSelectedTextFromPage());
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_define) {
            Toast.makeText(this, "Define " + getSelectedTextFromPage(), Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_highlight) {
            mHighLight = true;
            int currentPosition = mFolioPageViewPager.getCurrentItem();
            FolioPageFragment folioPageFragment = (FolioPageFragment) getFragment(currentPosition);
            folioPageFragment.highlight();
            startSupportActionMode(mActionModeCallback);
        } else if (item.getItemId() == R.id.menu_share) {
            AppUtil.share(this, getSelectedTextFromPage());
        }

        // This will likely always be true, but check it anyway, just in case
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        mActionMode = null;
        super.onActionModeFinished(mode);
    }

<<<<<<< HEAD
    private android.support.v7.view.ActionMode.Callback mActionModeCallback = new android.support.v7.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            Log.d("FolioActivity", "onCreateActionMode() ========>");
            // Inflate your own menu items
            menu.clear();
            mode.getMenuInflater().inflate(R.menu.menu_on_highlight, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            Log.d("FolioActivity", "onPrepareActionMode() ========>");
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
            Log.d("FolioActivity", "onActionItemClicked() ========>");
            return false;
        }

        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
            Log.d("FolioActivity", "onDestroyActionMode() ========>");
        }
    };*/


    /*private void parseSmil(){
        Collection<Resource> resourceArrayList= mBook.getResources().getAll();
        Iterator<Resource> iterator=resourceArrayList.iterator();
        while(iterator.hasNext()){
            Resource resource=iterator.next();
            String href=resource.getHref();
            if(href.contains("smil")){
                String content= null;
                try {
                    content = AppUtil.readerSmil(resource.getReader());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SmilFile smilFile=new SmilFile();
                try {
                    SmilParser smilParser=new SmilParser();
                    SequenceElement sequenceElement=smilParser.parse(content);
                    mAudioElementArrayList=sequenceElement.getAllAudioElementDepthFirst();
                  *//*  String str=mAudioElementArrayList.get(0).getSrc();*//*
                    mTextElementList=sequenceElement.getAllTextElementDepthFirst();

                    Log.e("smilfile","smilfile");
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.e("smilfile","smilfile");
            }


            if(href.contains(".mp3")){
                try {
                    AppUtil.saveFile(resource.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Spine spine = new Spine(mBook.getTableOfContents());
        List<SpineReference> spineReferences = spine.getSpineReferences();
        for(int i=0;i<spineReferences.size();i++){
            String temp=mTextElementList.get(0).getSrc();
            String arr[]=temp.split("#");
            Log.d("spine href 1*****",spineReferences.get(i).getResource().getHref());
            String[] arr2=spineReferences.get(i).getResource().getHref().split("/");
                if(arr2[1].equals(arr[1])){
                    mAudioPagePosition=i;
                }
        }
    }
*/



    private void parseSmil(){
        Collection<Resource> resourceArrayList= mBook.getResources().getAll();
        Iterator<Resource> iterator=resourceArrayList.iterator();
        while(iterator.hasNext()){
            Resource resource=iterator.next();
            String href=resource.getHref();
            if(href.contains("smil")){
                String content= null;
                try {
                    content = AppUtil.readerSmil(resource.getReader());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SmilFile smilFile=new SmilFile();
                try {
                    SmilParser smilParser=new SmilParser();
                    SequenceElement sequenceElement=smilParser.parse(content);
                    mAudioElementArrayList=sequenceElement.getAllAudioElementDepthFirst();
                  /*  String str=mAudioElementArrayList.get(0).getSrc();*/
                    mTextElementList=sequenceElement.getAllTextElementDepthFirst();

                    Log.e("smilfile","smilfile");
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.e("smilfile","smilfile");
            }


            if(href.contains(".mp3")){
                try {
                    AppUtil.saveFile(resource.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       if(mTextElementList!=null && mTextElementList.size()>0) {
           Spine spine = new Spine(mBook.getTableOfContents());
           List<SpineReference> spineReferences = spine.getSpineReferences();
           for (int i = 0; i < spineReferences.size(); i++) {
               String temp = mTextElementList.get(0).getSrc();
               String arr[] = temp.split("#");
               Log.d("spine href 1*****", spineReferences.get(i).getResource().getHref());
               String[] arr2 = spineReferences.get(i).getResource().getHref().split("/");
               if (arr2[1].equals(arr[1])) {
                   mAudioPagePosition = i;
               }
           }
       }
    }

    public  void setHighLight(int position,String style){
        String src=mTextElementList.get(position).getSrc();
        String temp[]=src.split("#");
        String textid=temp[1];
        ((FolioPageFragment)getFragment(mChapterPosition)).highLightString(textid,style);
    }

    public AudioElement getElement(int position){
        return  mAudioElementArrayList.get(position);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(audioView!=null){
            audioView.playerResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(audioView!=null){
            audioView.playerResume();
        }
    }
}
