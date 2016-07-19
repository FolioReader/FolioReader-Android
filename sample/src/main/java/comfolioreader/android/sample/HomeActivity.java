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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.folioreader.activity.FolioActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btn_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FolioActivity.class);
                //intent.putExtra(FolioActivity.INTENT_EPUB_ASSET_PATH, "The Silver Chair.epub");
                intent.putExtra(FolioActivity.INTENT_EPUB_ASSET_PATH, "The Silver Chair.epub");
                startActivity(intent);
            }
        });
    }

    /*private static class TestFragmentAdapter extends FragmentPagerAdapter {

        protected static final String[] CONTENT = new String[] { "This", "Is Is", "A A A", "Test", };

        public TestFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TestFragment.newInstance(CONTENT[position]);
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }

    }*/
}