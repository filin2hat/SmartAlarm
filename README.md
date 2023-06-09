## Smart Alarm v1.0

English:
Alarm Clock is an Android application that allows users to set alarms based on the time of sunrise
at their current location. The app uses the user's location to calculate the time of sunrise and 
allows the user to set an alarm for a specified number of hours and minutes after sunrise. The app 
also includes a real-time clock that updates every second and displays the current time.
The app is built using the Model-View-ViewModel (MVVM) architecture pattern and uses Kotlin as the 
primary programming language. The app also uses several Android Jetpack components,
including LiveData, ViewModel, and WorkManager. The app uses the FusedLocationProviderClient to get
the user's location and the SunTimes library to calculate the time of sunrise. The app also includes
a custom view for selecting the number of hours and minutes for the alarm.

Russian:
Alarm Clock - это приложение для Android, которое позволяет пользователям устанавливать будильники 
на основе времени восхода солнца в их текущем местоположении. Приложение использует местоположение
пользователя для расчета времени восхода солнца и позволяет пользователю установить будильник на
определенное количество часов и минут после восхода солнца. Приложение также включает часы реального
времени, которые обновляются каждую секунду и отображают текущее время.
Приложение построено с использованием архитектурного шаблона Model-View-ViewModel (MVVM) и использует
Kotlin в качестве основного языка программирования. Приложение также использует несколько компонентов
Android Jetpack, включая LiveData, ViewModel и WorkManager. Приложение использует 
FusedLocationProviderClient для получения местоположения пользователя и библиотеку SunTimes для 
расчета времени восхода солнца. Приложение также включает пользовательский вид для выбора количества
часов и минут для будильника.

### Technologies and Libraries Used / Использованные технологии и библиотеки
- Kotlin
- WorkManager
- AlarmManager
- Coroutines
- Google Play Services
