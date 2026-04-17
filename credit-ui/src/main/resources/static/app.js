const form = document.getElementById('credit-form');
const statusElement = document.getElementById('status');
let creditInputApplyUrl = '/api/credit/apply';

const configReady = (async () => {
  try {
    const response = await fetch('/ui-config');
    if (!response.ok) {
      return;
    }

    const config = await response.json();
    if (config.creditInputApplyUrl && typeof config.creditInputApplyUrl === 'string') {
      creditInputApplyUrl = config.creditInputApplyUrl;
    }
  } catch (error) {
    // Fall back to default route through Traefik.
  }
})();

form.addEventListener('submit', async event => {
  event.preventDefault();
  await configReady;
  statusElement.style.display = 'none';
  statusElement.className = 'status';
  statusElement.textContent = 'Submitting application...';
  statusElement.style.display = 'block';

  const payload = {
    name: document.getElementById('name').value.trim(),
    phoneNumber: document.getElementById('phone').value.trim(),
    aadharNumber: document.getElementById('aadhar').value.trim(),
    panNumber: document.getElementById('pan').value.trim(),
    creditCardNumber: document.getElementById('creditCard').value.trim(),
    creditCardExpiry: document.getElementById('expiry').value.trim(),
    cvc: document.getElementById('cvc').value.trim()
  };

  try {
    const response = await fetch(creditInputApplyUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    let result = {};
    const responseContentType = response.headers.get('content-type') || '';
    if (responseContentType.includes('application/json')) {
      result = await response.json();
    } else {
      const responseText = await response.text();
      if (responseText && responseText.trim()) {
        result = { message: responseText };
      }
    }

    if (!response.ok) {
      statusElement.className = 'status error';
      statusElement.textContent = result.message || `Unable to submit application (HTTP ${response.status}).`;
      return;
    }

    statusElement.className = 'status';
    statusElement.textContent = `Application submitted successfully. Your application number is ${result.applicationNumber}.`;
    form.reset();
  } catch (error) {
    statusElement.className = 'status error';
    statusElement.textContent = `Failed to contact the credit-input service. ${error.message}`;
  }
});
