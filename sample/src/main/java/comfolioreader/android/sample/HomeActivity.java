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
package comfolioreader.android.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.folioreader.model.Highlight;
import com.folioreader.util.FolioReader;
import com.folioreader.util.OnHighlightCreateListener;

public class HomeActivity extends AppCompatActivity implements OnHighlightCreateListener {

    private FolioReader folioReader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        folioReader = new FolioReader(this);
        folioReader.setOnHighlightCreateListener(this);
        findViewById(R.id.btn_assest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folioReader.openBook("file:///android_asset/adventures.epub");
            }
        });

        findViewById(R.id.btn_raw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folioReader.openBook(R.raw.barrett);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        folioReader.unSubscribe();
    }

    @Override
    public void onCreateHighlight(Highlight highlight, Highlight.HighLightAction type) {
    }
}