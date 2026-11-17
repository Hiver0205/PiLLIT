<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="docs/images/logo-dark.png" />
  <source media="(prefers-color-scheme: light)" srcset="docs/images/logo-light.png" />
  <img src="docs/images/logo-light.png" alt="PiLLIT ScanEat 로고" width="360" />
</picture>

# PiLLIT · ScanEat

### 💊 찍으면 알려주는 약물 알레르기 확인 앱

약 포장지를 카메라로 찍으면 OCR로 주성분을 읽고,<br/>
내 알레르기와 비교해 **먹어도 되는 약인지** 바로 알려줍니다.

<br/>

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
<br/>
![CameraX](https://img.shields.io/badge/CameraX-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![OpenCV](https://img.shields.io/badge/OpenCV-5C3EE8?style=for-the-badge&logo=opencv&logoColor=white)

![minSdk](https://img.shields.io/badge/minSdk-24-1F2A44?style=flat-square)
![platform](https://img.shields.io/badge/platform-Android-1F2A44?style=flat-square)
![purpose](https://img.shields.io/badge/purpose-study%20%26%20research-1F2A44?style=flat-square)

<br/>

[소개](#-프로젝트-소개) ·
[주요 기능](#-주요-기능) ·
[동작 방식](#-동작-방식) ·
[기술 스택](#-기술-스택) ·
[폴더 구성](#-폴더-구성) ·
[DB 구조](#-firestore-구조) ·
[OCR 평가](#-ocr-평가-파이프라인) ·
[실행 방법](#-실행-방법) ·
[결과 화면](#-결과-화면)

</div>

<br/>

## 📖 프로젝트 소개

### 왜 만들었나요

약물 알레르기가 있는 사람은 약을 먹기 전마다 성분표를 직접 읽고 확인해야 합니다. 하지만 이 과정에는 불편한 점이 많습니다.

- **성분명이 어렵습니다.** 포장지의 성분표는 글자가 작고, 낯선 화학명으로 적혀 있어 놓치기 쉽습니다.
- **제품명만으로는 알 수 없습니다.** 같은 주성분이 서로 다른 제품명으로 팔리기 때문에, 처음 보는 약이라도 내가 피해야 할 성분이 들어 있을 수 있습니다.
- **사람마다 위험한 정도가 다릅니다.** 같은 성분이라도 가볍게 지나가는 사람이 있고, 절대 먹으면 안 되는 사람이 있습니다.

### 어떻게 해결하나요

**PiLLIT**은 이 확인 과정을 사진 한 장으로 줄입니다. 내 알레르기 성분과 증상 정도를 한 번만 등록해 두면, 그다음부터는 약 포장지를 찍기만 하면 됩니다.

1. **등록** — 의약품 성분 목록에서 내 알레르기 성분을 고르고, 성분마다 증상이 가벼운지 심한지 표시합니다.
2. **스캔** — 약 포장지나 성분표를 카메라로 찍거나 갤러리에서 사진을 고릅니다.
3. **인식** — OCR 서버가 사진에서 글자를 읽고, 앱이 의약품 DB와 대조해 주성분을 찾아냅니다.
4. **확인** — 같은 성분의 제품 목록에서 내 약을 고르면, **DANGER / WARNING / SAFETY** 중 하나로 결과를 알려주고 부작용·효능·주의사항을 함께 보여줍니다.

### 이런 점이 다릅니다

| | |
|---|---|
| 🧪 **제품명이 아닌 주성분 기준** | 제품 이름이 달라도 같은 성분이면 잡아냅니다. |
| 🚦 **나에게 맞춘 3단계 경고** | 내가 등록한 증상 정도에 따라 같은 약도 다른 결과가 나옵니다. |
| 📷 **입력 없이 사진 한 장** | 성분명을 직접 타이핑하거나 검색할 필요가 없습니다. |
| 💊 **결과와 약 정보를 한 화면에** | 위험 여부와 함께 부작용·효능·주의사항을 바로 확인합니다. |
| 🔁 **실패해도 막히지 않는 흐름** | 글자를 못 읽으면 재촬영을, DB에 없는 약이면 별도 안내 화면을 보여줍니다. |

### 누구를 위한 앱인가요

약물 알레르기가 있어 약을 살 때마다 성분을 확인해야 하는 사람, 그리고 가족의 약을 대신 챙기는 보호자를 위한 앱입니다.

<br/>

## ✨ 주요 기능

<table>
  <tr>
    <td width="50%" valign="top">
      <h4>👤 회원가입 · 로그인</h4>
      Firebase Authentication 기반 이메일 계정으로 내 정보를 안전하게 보관합니다.
    </td>
    <td width="50%" valign="top">
      <h4>📝 알레르기 등록</h4>
      의약품 성분 목록에서 검색해 내 알레르기 성분을 고릅니다.
    </td>
  </tr>
  <tr>
    <td valign="top">
      <h4>🌡️ 증상 정도 설정</h4>
      성분마다 가벼움(mild) 또는 심함(severe)을 지정해 경고 수준을 나눕니다.
    </td>
    <td valign="top">
      <h4>📷 약 스캔</h4>
      카메라로 찍거나 갤러리 이미지를 골라 OCR 서버로 보냅니다.
    </td>
  </tr>
  <tr>
    <td valign="top">
      <h4>🔎 성분 매칭</h4>
      OCR 텍스트에서 의약품 DB의 주성분을 찾아내고, 같은 성분의 제품 중 내 약을 선택합니다.
    </td>
    <td valign="top">
      <h4>💊 약 정보 확인</h4>
      부작용, 효능, 주의사항을 확대해서 볼 수 있는 화면으로 제공합니다.
    </td>
  </tr>
</table>

<br/>

## 🔄 동작 방식

```mermaid
flowchart LR
    A["📷 촬영 / 이미지 선택"] --> B["🖥️ OCR 서버<br/>텍스트 추출"]
    B --> C{"텍스트를<br/>읽었나요?"}
    C -- 아니오 --> F1["🔁 재촬영 안내"]
    C -- 예 --> D{"DB에 있는<br/>주성분인가요?"}
    D -- 아니오 --> F2["❓ 찾을 수 없음"]
    D -- 예 --> E["📋 제품 선택"]
    E --> G{"내 알레르기와<br/>비교"}
    G -- "severe (2)" --> R1["🔴 DANGER"]
    G -- "mild (1)" --> R2["🟠 WARNING"]
    G -- "미등록" --> R3["🟢 SAFETY"]
```

1. 앱이 이미지를 JPEG로 압축해 OCR 서버(`/ocr`)에 `multipart/form-data`로 전송합니다.
2. 받은 텍스트에서 한글·영문·숫자만 남겨 정규화한 뒤, Firestore `drugs_v2` 컬렉션의 `주성분`이 포함되어 있는지 찾습니다.
3. 일치하는 성분의 제품 목록을 보여주고, 사용자가 실제 제품을 고릅니다.
4. 사용자의 알레르기 등록 정보(`prefix`)와 비교해 결과를 판정하고, 약의 상세 정보를 함께 보여줍니다.

### 화면으로 보는 스캔 과정

<div align="center">

<table>
  <tr>
    <td align="center" width="25%"><img src="docs/images/scan_1_allergy.png" alt="알레르기 등록 화면" width="190" /></td>
    <td align="center" width="25%"><img src="docs/images/scan_2_method.png" alt="스캔 방법 선택 화면" width="190" /></td>
    <td align="center" width="25%"><img src="docs/images/scan_3_product.png" alt="제품 선택 화면" width="190" /></td>
    <td align="center" width="25%"><img src="docs/images/scan_4_result.png" alt="결과 화면" width="190" /></td>
  </tr>
  <tr>
    <td align="center"><b>① 알레르기 등록</b><br/><sub>성분을 검색해 체크</sub></td>
    <td align="center"><b>② 스캔 방법 선택</b><br/><sub>카메라 촬영 또는 이미지 업로드</sub></td>
    <td align="center"><b>③ 제품 선택</b><br/><sub>인식된 성분의 제품 중 내 약 선택</sub></td>
    <td align="center"><b>④ 결과 확인</b><br/><sub>위험도와 약 정보 표시</sub></td>
  </tr>
</table>

<sub>앱 레이아웃을 바탕으로 재구성한 예시 화면이며, 성분·제품명은 예시입니다.</sub>

</div>

<br/>

## 🛠 기술 스택

| 구분 | 사용 기술 |
|:---:|---|
| **앱** | Android (Java) · Fragment 기반 단일 Activity 구조 · Material Components |
| **카메라** | CameraX |
| **인증** | Firebase Authentication |
| **데이터베이스** | Cloud Firestore |
| **파일 저장** | Firebase Storage |
| **OCR** | 별도 OCR 서버(HTTP API) · `HttpURLConnection` 통신 |
| **평가** | Python · OpenCV · Pillow · NumPy · Matplotlib · Requests |

<br/>

## 📁 폴더 구성

<details>
<summary><b>전체 구조 펼치기</b></summary>

<br/>

```
PiLLIT/
├── README.md
├── docs/images/                     # README용 이미지 (로고·화면 예시)
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

</details>

<br/>

## 🗄 Firestore 구조

```mermaid
erDiagram
    USERS ||--|| MY_ALLERGIES : "allergies/myAllergies"
    USERS ||--o{ SELECTED_PRODUCTS : "selectedProducts"
    DRUGS_V2 ||--o{ SELECTED_PRODUCTS : "제품명"

    USERS {
        string name
        string email
    }
    MY_ALLERGIES {
        array selectedIngredients "선택한 알레르기 성분"
        map mild "성분별 가벼움 여부"
        map severe "성분별 심함 여부"
        map prefix "성분별 판정 값 0·1·2"
    }
    SELECTED_PRODUCTS {
        string product "스캔 후 선택한 제품"
    }
    DRUGS_V2 {
        string 제품명
        string 주성분
        string 부작용
        string 효능
        string 주의사항
    }
```

<br/>

## 📊 OCR 평가 파이프라인

저장소 루트의 `Add OCR evaluation pipeline` 파일은 OCR 서버의 성분 인식 성능을 측정하는 Python 스크립트입니다.

```mermaid
flowchart LR
    S1["원본 이미지<br/>test_images/"] --> S2["이미지 증강<br/>약한 변형 8 + 중간 변형 2"]
    S2 --> S3["OCR API 호출"]
    S3 --> S4["정답과 비교<br/>Levenshtein 유사도"]
    S4 --> S5["그래프 · CSV 저장"]
```

| 단계 | 내용 |
|:---:|---|
| **증강** | 밝기 · 대비 · 블러 · 회전 · 크롭 · 노이즈 · JPEG 압축을 무작위로 적용 |
| **비교** | OCR 텍스트를 정답 길이만큼 슬라이딩하며 Levenshtein 거리 기반 유사도의 최댓값 계산 |
| **결과** | 이미지별 막대그래프 · 평균 유사도 도넛 그래프 · `ingredient_results.csv` |

<details>
<summary><b>실행 방법 펼치기</b></summary>

<br/>

`test_images/`(이미지)와 `ground_truth/`(같은 이름의 `.txt` 정답 파일) 폴더를 먼저 준비합니다.

```bash
pip install opencv-python pillow numpy matplotlib requests
python "Add OCR evaluation pipeline"
```

</details>

<br/>

## 🚀 실행 방법

```bash
git clone https://github.com/Hiver0205/PiLLIT.git
```

1. Android Studio에서 `pillitscaneatYSM` 폴더를 엽니다.
2. Gradle 동기화가 끝나면 에뮬레이터나 실제 기기에서 실행합니다. (minSdk 24)

> [!NOTE]
> 앱은 Firebase 프로젝트와 OCR 서버에 연결되어야 동작합니다. 직접 구성하려면 Firebase에서 Authentication · Firestore · Storage를 켜고 `app/google-services.json`을 본인 것으로 교체한 뒤, `drugs_v2` 컬렉션에 의약품 데이터를 넣어야 합니다.

<br/>

## 📱 결과 화면

스캔이 끝나면 내 알레르기 등록 정보에 따라 세 가지 화면 중 하나가 표시됩니다. 같은 약이라도 등록한 증상 정도에 따라 결과가 달라집니다.

<div align="center">

<table>
  <tr>
    <td align="center" width="33%"><img src="docs/images/result_danger.png" alt="DANGER 결과 화면" width="240" /></td>
    <td align="center" width="33%"><img src="docs/images/result_warning.png" alt="WARNING 결과 화면" width="240" /></td>
    <td align="center" width="33%"><img src="docs/images/result_safety.png" alt="SAFETY 결과 화면" width="240" /></td>
  </tr>
  <tr>
    <td align="center"><b>🔴 DANGER</b><br/><sub>심한 알레르기로 등록한 성분<br/>복용하지 마세요</sub></td>
    <td align="center"><b>🟠 WARNING</b><br/><sub>가벼운 알레르기로 등록한 성분<br/>복용에 주의하세요</sub></td>
    <td align="center"><b>🟢 SAFETY</b><br/><sub>등록한 알레르기에 없는 성분<br/>안전해요</sub></td>
  </tr>
</table>

<sub>앱 레이아웃을 바탕으로 재구성한 예시 화면이며, 성분명과 부작용 문구는 예시입니다.</sub>

</div>

각 화면에는 위험도와 함께 주성분, 부작용이 표시되고, **더보기**를 누르면 효능과 주의사항을 확대해서 볼 수 있습니다.

<br/>

---

> [!WARNING]
> 본 앱은 학습·연구 목적의 **보조 도구**이며, 의사·약사의 전문적인 판단을 대체하지 않습니다. OCR 인식 오류나 DB에 없는 약이 있을 수 있으므로, 실제 복용 전에는 반드시 전문가와 상담하세요.

<div align="center">
<br/>
<sub>Made with 💊 by PiLLIT Team</sub>
</div>
