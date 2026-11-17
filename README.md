# PiLLIT (ScanEat)

> **약 포장지를 찍으면 내 알레르기 성분이 들어 있는지 알려주는 안드로이드 앱**
> An Android app that scans medicine packaging with OCR and warns you if it contains an ingredient you are allergic to.

---

## 프로젝트 소개

약물 알레르기가 있는 사람은 약을 먹기 전에 성분표를 직접 읽고 확인해야 합니다. 하지만 성분명은 작고 낯선 글자로 적혀 있어 놓치기 쉽습니다.

**PiLLIT**은 이 확인 과정을 대신합니다. 미리 내 알레르기 성분과 증상 정도를 등록해 두고, 약 포장지나 성분표를 카메라로 찍으면 OCR로 주성분을 읽어 **DANGER / WARNING / SAFETY** 세 단계로 알려줍니다. 해당 약의 부작용·효능·주의사항도 함께 보여줍니다.

---

## 이런 걸 할 수 있어요

- 👤 **회원가입·로그인** — Firebase Authentication 기반 이메일 계정
- 📝 **알레르기 등록** — 의약품 성분 목록에서 검색해 내 알레르기 성분을 선택
- 🌡️ **증상 정도 설정** — 성분별로 가벼움(mild) / 심함(severe)을 지정
- 📷 **약 스캔** — 카메라 촬영 또는 갤러리 이미지를 OCR 서버로 전송
- 🔎 **성분 매칭** — OCR 텍스트에서 의약품 DB의 주성분을 찾아내고, 같은 성분의 제품 목록에서 내 약을 선택
- 🚦 **위험도 판정** — 내 알레르기 정보와 비교해 DANGER / WARNING / SAFETY 표시
- 💊 **약 정보 확인** — 부작용, 효능, 주의사항을 확대 가능한 화면으로 제공

---

## 어떻게 동작하나요

```
촬영/이미지 선택 → OCR 서버(텍스트 추출) → 주성분 매칭(Firestore) → 제품 선택 → 내 알레르기와 비교 → 위험도 + 약 정보 표시
```

1. 앱이 이미지를 JPEG로 압축해 OCR 서버(`/ocr`)에 `multipart/form-data`로 전송합니다.
2. 받은 텍스트에서 한글·영문·숫자만 남겨 정규화한 뒤, Firestore `drugs_v2` 컬렉션의 `주성분`이 포함되어 있는지 찾습니다.
3. 일치하는 성분의 제품 목록을 보여주고, 사용자가 실제 제품을 고릅니다.
4. 사용자의 알레르기 등록 정보(`prefix`)와 비교해 결과를 판정합니다.

| 등록 상태 | 값 | 결과 |
|---|---|---|
| 심함(severe) | `2` | 🔴 DANGER — 복용하지 마세요 |
| 가벼움(mild) | `1` | 🟠 WARNING — 복용에 주의하세요 |
| 미등록 | 없음 / `0` | 🟢 SAFETY — 안전해요 |

텍스트를 읽지 못하면 재촬영 안내 화면으로, DB에 없는 성분·제품이면 "찾을 수 없음" 화면으로 이동합니다.

---

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| 앱 | Android (Java), Fragment 기반 단일 Activity 구조, Material Components |
| 카메라 | CameraX |
| 인증 | Firebase Authentication |
| 데이터베이스 | Cloud Firestore |
| 파일 저장 | Firebase Storage |
| OCR | 별도 OCR 서버(HTTP API)와 `HttpURLConnection`으로 통신 |
| 평가 | Python (OpenCV, Pillow, NumPy, Matplotlib, Requests) |

---

## Firestore 구조

```
drugs_v2/{문서}
  ├── 제품명, 제품명_lower
  ├── 주성분
  └── 부작용, 효능, 주의사항

users/{uid}
  ├── name, email
  ├── allergies/myAllergies
  │     ├── selectedIngredients   # 선택한 알레르기 성분 목록
  │     ├── mild / severe         # 성분별 증상 정도
  │     └── prefix                # 성분별 판정 값 (0 / 1 / 2)
  └── selectedProducts/{성분명}    # 스캔 후 선택한 제품
```

---

## OCR 평가 파이프라인

저장소 루트의 `Add OCR evaluation pipeline` 파일은 OCR 서버의 성분 인식 성능을 측정하는 Python 스크립트입니다.

1. `test_images/`의 원본 이미지를 불러옵니다.
2. 이미지마다 10장을 증강합니다 (약한 변형 8장 + 중간 변형 2장: 밝기·대비·블러·회전·크롭·노이즈·JPEG 압축).
3. OCR API 결과와 `ground_truth/`의 정답 성분명을 비교합니다. OCR 텍스트를 정답 길이만큼 슬라이딩하며 **Levenshtein 거리 기반 유사도**의 최댓값을 구합니다.
4. 이미지별 막대그래프, 평균 유사도 도넛 그래프, 결과 CSV를 저장합니다.

```bash
pip install opencv-python pillow numpy matplotlib requests
python "Add OCR evaluation pipeline"
```

실행 전에 `test_images/`(이미지)와 `ground_truth/`(같은 이름의 `.txt` 정답 파일) 폴더를 준비해야 합니다.

---

## 실행 방법

1. 저장소를 내려받습니다.
   ```bash
   git clone https://github.com/Hiver0205/PiLLIT.git
   ```
2. Android Studio에서 `pillitscaneatYSM` 폴더를 엽니다.
3. Gradle 동기화가 끝나면 에뮬레이터나 실제 기기에서 실행합니다. (minSdk 24)

앱은 Firebase 프로젝트와 OCR 서버에 연결되어야 동작합니다. 직접 구성하려면 Firebase 프로젝트에서 Authentication·Firestore·Storage를 켜고 `app/google-services.json`을 본인 것으로 교체한 뒤, `drugs_v2` 컬렉션에 의약품 데이터를 넣어야 합니다.

---

## 폴더 구성

```
PiLLIT/
├── Add OCR evaluation pipeline      # OCR 평가 스크립트 (Python)
└── pillitscaneatYSM/                # 안드로이드 프로젝트
    └── app/src/main/
        ├── java/com/example/pillit_scaneat/
        │   ├── SplashActivity, StartActivity          # 시작 화면
        │   ├── LoginActivity, SignupActivity          # 로그인·회원가입
        │   ├── MainActivity, HomeFragment             # 메인·내비게이션
        │   ├── AllergyFragment                        # 알레르기 성분 선택
        │   ├── AllergySymptomFragment                 # 증상 정도 설정
        │   ├── ScanMenuFragment, ScanFragment         # 스캔 메뉴·촬영/업로드
        │   ├── ScanCameraFragment, ImageScanFragment  # 카메라·이미지 스캔
        │   ├── ProductSelectFragment                  # 제품 선택
        │   ├── ResultFragment                         # 위험도 결과
        │   ├── ProductDetailFragment                  # 약 상세 정보
        │   ├── OCRFailFragment, NotFoundFragment      # 실패·미등록 안내
        │   └── UserInfoFragment, ChangePasswordActivity  # 내 정보
        └── res/                                       # 레이아웃·이미지·문자열
```

---

> ⚠️ 본 앱은 학습·연구 목적의 **보조 도구**이며, 의사·약사의 전문적인 판단을 대체하지 않습니다. OCR 인식 오류나 DB에 없는 약이 있을 수 있으므로, 실제 복용 전에는 반드시 전문가와 상담하세요.
