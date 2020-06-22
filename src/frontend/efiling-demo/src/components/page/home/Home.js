import React, { useState } from "react";
import PropTypes from "prop-types";
import axios from "axios";
import Header, { Footer, Input } from "shared-components";
import { Button } from "../../base/button/Button";

import "../page.css";

const urlBody = {
  documentProperties: {
    type: "string",
    subType: "string",
    submissionAccess: {
      url: "string",
      verb: "GET",
      headers: {
        additionalProp1: "string",
        additionalProp2: "string",
        additionalProp3: "string"
      }
    }
  },
  navigation: {
    success: {
      url: "string"
    },
    error: {
      url: "string"
    },
    cancel: {
      url: "string"
    }
  }
};

const input = {
  label: "Account GUID",
  id: "textInputId",
  styling: "editable_white",
  isRequired: true,
  placeholder: "77da92db-0791-491e-8c58-1a969e67d2fa"
};

const generateUrl = (accountGuid, setErrorExists) => {
  const updatedUrlBody = { ...urlBody, userId: accountGuid };

  axios
    .post(`/submission/generateUrl`, updatedUrlBody)
    .then(({ data: { efilingUrl } }) => {
      window.open(efilingUrl, "_self");
    })
    .catch(() => {
      setErrorExists(true);
    });
};

export default function Home({ page: { header } }) {
  const [errorExists, setErrorExists] = useState(false);
  const [accountGuid, setAccountGuid] = useState(null);

  return (
    <main>
      <Header header={header} />
      <div className="page">
        <div className="content col-md-10">
          <Input input={input} onChange={setAccountGuid} />
          <br />
          <Button
            onClick={() => generateUrl(accountGuid, setErrorExists)}
            label="Generate URL"
          />
          <br />
          {errorExists && (
            <p className="error">
              An error occurred while generating the URL. Please try again.
            </p>
          )}
        </div>
      </div>
      <Footer />
    </main>
  );
}

Home.propTypes = {
  page: PropTypes.shape({
    header: PropTypes.shape({
      name: PropTypes.string.isRequired
    }).isRequired
  }).isRequired
};
