package com.practicum.showandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int VOLUME_DOWN_CLICK_COUNT = 3;
    private int volumeDownClickCounter = 0;
    private static final int PICK_FOLDER_REQUEST_CODE = 1;
    private SlideshowAdapter slideshowAdapter;

    private Button startButton;
    private Button frequencyButton;
    private Button imagesButton;
    private ImageView imageView;

    private long slideshowDelay = 5000; // Значение по умолчанию - 5 секунд
    private List<Uri> images = new ArrayList<>(); // Список фотографий в папке

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        frequencyButton = findViewById(R.id.frequencyButton);
        imagesButton = findViewById(R.id.imagesButton);
        imageView = findViewById(R.id.imageView);

        // Отключаем кнопку "Начать" до выбора папки
        startButton.setEnabled(false);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!images.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, SlideshowActivity.class);
                    intent.putParcelableArrayListExtra("images", new ArrayList<>(images));
                    intent.putExtra("slideshowDelay", slideshowDelay);
                    startActivity(intent);
                } else {
                    showToast("Выберите папку с изображениями");
                }
            }
        });

        frequencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFrequencyMenu();
            }
        });

        imagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileMenu();
            }
        });
    }

    private void openFrequencyMenu() {
        PopupMenu popupMenu = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            popupMenu = new PopupMenu(MainActivity.this, frequencyButton);
        }
        popupMenu.getMenuInflater().inflate(R.menu.frequency_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_slow:
                        slideshowDelay = 8000; // 8 секунд
                        showToast("Выбран режим 'Медленно' (8 секунд)");
                        break;
                    case R.id.menu_normal:
                        slideshowDelay = 5000; // 5 секунд
                        showToast("Выбран режим 'Нормально' (5 секунд)");
                        break;
                    case R.id.menu_fast:
                        slideshowDelay = 3000; // 3 секунды
                        showToast("Выбран режим 'Быстро' (3 секунды)");
                        break;
                }

                if (slideshowAdapter != null) {
                    slideshowAdapter.setSlideshowDelay(slideshowDelay);
                }


                return true;
            }
        });

        popupMenu.show();
    }

    private void openFileMenu() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FOLDER_REQUEST_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, PICK_FOLDER_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FOLDER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
            // Обрабатываем выбранную папку
            if (pickedDir != null) {
                // Проверяем, является ли выбранный файл папкой
                if (pickedDir.isDirectory()) {
                    // Получаем список файлов в папке
                    DocumentFile[] files = pickedDir.listFiles();
                    // Проверяем, что список файлов не пустой
                    if (files != null && files.length > 0) {
                        images.clear();
                        for (DocumentFile file : files) {
                            images.add(file.getUri());
                        }
                        // Включаем кнопку "Начать" после выбора папки
                        startButton.setEnabled(true);
                    } else {
                        showToast("Выбранная папка не содержит фотографий");
                        // Выключаем кнопку "Начать" при отсутствии фотографий
                        startButton.setEnabled(false);
                    }
                } else {
                    showToast("Выбран не папка");
                    // Выключаем кнопку "Начать" при выборе неправильного типа файла
                    startButton.setEnabled(false);
                }
            } else {
                showToast("Ошибка при выборе папки");
                // Выключаем кнопку "Начать" при ошибке выбора папки
                startButton.setEnabled(false);
            }
        }
    }

    // Метод для отображения сообщения в виде всплывающей подсказки
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Переопределение метода для обработки события нажатия на кнопку управления громкостью
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            volumeDownClickCounter++;
            if (volumeDownClickCounter == VOLUME_DOWN_CLICK_COUNT) {
                finish();
                return true;
            }
        } else {
            volumeDownClickCounter = 0;
        }
        return super.onKeyDown(keyCode, event);
    }
}
